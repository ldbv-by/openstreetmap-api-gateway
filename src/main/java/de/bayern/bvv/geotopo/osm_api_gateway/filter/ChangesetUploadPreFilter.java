package de.bayern.bvv.geotopo.osm_api_gateway.filter;

import de.bayern.bvv.geotopo.osm_api_gateway.component.OsmNotificationServiceClient;
import de.bayern.bvv.geotopo.osm_api_gateway.component.OsmQualityFrameworkClient;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityHubResultDto;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityServiceErrorDto;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityServiceResultDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@Slf4j
public class ChangesetUploadPreFilter extends AbstractGatewayFilterFactory<ChangesetUploadPreFilter.Config> {

    private final OsmQualityFrameworkClient osmQualityFrameworkClient;
    private final OsmNotificationServiceClient osmNotificationServiceClient;

    public ChangesetUploadPreFilter(OsmQualityFrameworkClient osmQualityFrameworkClient,
                                    OsmNotificationServiceClient osmNotificationServiceClient) {
        super(Config.class);
        this.osmQualityFrameworkClient = osmQualityFrameworkClient;
        this.osmNotificationServiceClient = osmNotificationServiceClient;
    }

    /**
     * Validates each changeset via openstreetmap-quality-framework:
     * forwards valid requests to OSM-API, rejects invalid ones.
     */
    @Override
    public GatewayFilter apply(Config config) {

        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {
            // Read changeset from body
            Mono<String> changeset = this.readChangesetFromBody(exchange)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty request body")));

            // Send changeset to Quality-Hub
            Mono<QualityHubResultDto> qualityHubResultDto = changeset.flatMap(cs -> {
                long changesetId = this.readChangesetId(exchange);
                log.info("changesetId: {}\n{}", changesetId, cs);
                return this.osmQualityFrameworkClient.sendChangesetToQualityHub(changesetId, cs);
            });

            // Check response from Quality-Hub
            // Reject request on errors, otherwise forward modified changeset to OSM-API
            return qualityHubResultDto.flatMap(qualityResponseDto -> {
                if (qualityResponseDto.isValid()) {
                    return this.redirectToOsmApi(exchange, chain, qualityResponseDto);
                } else {
                    Mono<Void> notify = Mono.empty();

                    if (this.osmNotificationServiceClient.isConfigured()) {
                        // Send error to openstreetmap-notification-service
                        notify = this.osmNotificationServiceClient.sendQualityHubResult(qualityResponseDto);
                    }

                    return notify.then(this.rejectChangeset(exchange, qualityResponseDto));
                }
            });
        };
    }


    /**
     * Read changeset id from url.
     */
    public Long readChangesetId(ServerWebExchange exchange) {
        Map<String, String> vars = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
        String changesetId = vars.get("changeset-id");

        if (changesetId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing changeset-id in url");
        }

        try {
            return Long.valueOf(changesetId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid changeset-id in url: " + changesetId, ex);
        }
    }


    /**
     * Read changeset from body.
     */
    public Mono<String> readChangesetFromBody(ServerWebExchange exchange) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(buf -> {
                    try {
                        byte[] bytes = new byte[buf.readableByteCount()];
                        buf.read(bytes);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } finally {
                        DataBufferUtils.release(buf);
                    }
                });
    }


    /**
     * Changeset is valid. Redirect to OSM-API.
     */
    private Mono<Void> redirectToOsmApi(ServerWebExchange exchange,
                                        GatewayFilterChain chain,
                                        QualityHubResultDto qualityHubResultDto) {

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate().build();
        ServerWebExchange modifiedExchange = new ServerWebExchangeDecorator(exchange) {
            @Override
            public ServerHttpRequest getRequest() {
                return new ServerHttpRequestDecorator(modifiedRequest) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        byte[] bytes = qualityHubResultDto.changesetXml().getBytes(StandardCharsets.UTF_8);
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        return Flux.just(buffer);
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        HttpHeaders headers = new HttpHeaders();
                        headers.putAll(super.getHeaders());
                        headers.setContentLength(qualityHubResultDto.changesetXml().getBytes(StandardCharsets.UTF_8).length);
                        headers.setContentType(MediaType.APPLICATION_XML);
                        return headers;
                    }
                };
            }
        };

        return chain.filter(modifiedExchange);
    }


    /**
     * Changeset is invalid. Reject changeset.
     */
    private Mono<Void> rejectChangeset(ServerWebExchange exchange, QualityHubResultDto qualityHubResult) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().set("Content-Type", MediaType.TEXT_PLAIN_VALUE);
        response.getHeaders().set("Error", this.getRejectMessageAsHtml(qualityHubResult));

        byte[] bytes = this.getRejectMessageAsPlainText(qualityHubResult).getBytes(StandardCharsets.UTF_8);
        var buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer))
                .doOnError(ex -> DataBufferUtils.release(buffer));
    }


    /**
     * Get reject message as plain text.
     */
    private String getRejectMessageAsPlainText(QualityHubResultDto qualityHubResult) {
        StringBuilder rejectMessage = new StringBuilder();
        for (QualityServiceResultDto qualityServiceResult : qualityHubResult.qualityServiceResults()) {
            if (!qualityServiceResult.isValid()) {
                for (QualityServiceErrorDto error : qualityServiceResult.errors()) {
                    rejectMessage.append("\n • ").append(error.errorText());
                }
            }
        }

        return rejectMessage.toString();
    }


    /**
     * Get reject message as html text.
     */
    private String getRejectMessageAsHtml(QualityHubResultDto qualityHubResult) {
        StringBuilder rejectMessage = new StringBuilder("<h1 style=\"color: red; font-size: 13pt;\"><br><b>Changeset rejected ...</b></h1>");
        for (QualityServiceResultDto qualityServiceResult : qualityHubResult.qualityServiceResults()) {
            if (!qualityServiceResult.isValid()) {
                for (QualityServiceErrorDto error : qualityServiceResult.errors()) {
                    rejectMessage.append("<br>- ").append(error.errorText());
                }
            }
        }

        return rejectMessage.toString();
    }


    /**
     * Specific filter configuration.
     */
    @Data
    public static class Config {}
}
