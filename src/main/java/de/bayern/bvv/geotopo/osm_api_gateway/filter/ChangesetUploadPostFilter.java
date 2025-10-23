package de.bayern.bvv.geotopo.osm_api_gateway.filter;

import de.bayern.bvv.geotopo.osm_api_gateway.component.OsmQualityFrameworkClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class ChangesetUploadPostFilter extends AbstractGatewayFilterFactory<ChangesetUploadPostFilter.Config> {

    private final OsmQualityFrameworkClient osmQualityFrameworkClient;

    public ChangesetUploadPostFilter(OsmQualityFrameworkClient osmQualityFrameworkClient) {
        super(Config.class);
        this.osmQualityFrameworkClient = osmQualityFrameworkClient;
    }

    /**
     * Updates OpenStreetMap geometries with the finalized changeset.
     * Sets the changeset state to "finished".
     */
    @Override
    public GatewayFilter apply(Config config) {

        return (ServerWebExchange exchange, GatewayFilterChain chain) ->
            chain.filter(exchange).then(Mono.fromRunnable(() -> {

                if (exchange.getResponse().getStatusCode() == HttpStatus.OK) {

                    Long changesetId = this.readChangesetId(exchange);

                    if (changesetId != null) {
                        this.osmQualityFrameworkClient.finishChangeset(changesetId);
                    }
                }

            }));
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
     * Specific filter configuration.
     */
    @Data
    public static class Config {}
}
