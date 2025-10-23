package de.bayern.bvv.geotopo.osm_api_gateway.component;

import de.bayern.bvv.geotopo.osm_api_gateway.config.ApiGatewayConfig;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityHubResultDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OsmQualityFrameworkClient {
    private final WebClient webClient;

    /**
     * Create Quality-Hub Client.
     */
    public OsmQualityFrameworkClient(ApiGatewayConfig apiGatewayConfig, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(apiGatewayConfig.getOpenstreetmapQualityFrameworkUrl())
                .build();
    }

    /**
     * Sends changeset to the Quality-Hub.
     */
    public Mono<QualityHubResultDto> sendChangesetToQualityHub(Long changesetId, String changeset){
        return this.webClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/osm-quality-framework/v1/quality-hub/check/changeset/{changesetId}")
                        .build(changesetId))
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(changeset)
                .retrieve()
                .bodyToMono(QualityHubResultDto.class);
    }

    /**
     * Finish changeset.
     */
    public void finishChangeset(Long changesetId){
        this.webClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/osm-quality-framework/v1/quality-hub/finish/changeset/{changesetId}")
                        .build(changesetId))
                .retrieve()
                .toBodilessEntity()
                .subscribe();
    }
}
