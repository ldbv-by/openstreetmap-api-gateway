package de.bayern.bvv.geotopo.osm_api_gateway.component;

import de.bayern.bvv.geotopo.osm_api_gateway.config.ApiGatewayConfig;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityHubResultDto;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityServiceErrorDto;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.QualityServiceResultDto;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.notification.NewNotification;
import de.bayern.bvv.geotopo.osm_api_gateway.dto.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class OsmNotificationServiceClient {

    private final WebClient webClient;
    private final ApiGatewayConfig apiGatewayConfig;

    private static final String NOTIFICATION_GROUP_PREFIX = "Changeset ";
    private static final String NOTIFICATION_COLOR = "#FF0000";
    /**
     * Create Quality-Hub Client.
     */
    public OsmNotificationServiceClient(ApiGatewayConfig apiGatewayConfig, WebClient.Builder webClientBuilder) {
        this.apiGatewayConfig = apiGatewayConfig;

        this.webClient = webClientBuilder
                .baseUrl(apiGatewayConfig.getOpenstreetmapNotificationServiceUrl())
                .build();
    }

    /**
     * Check if OpenStreetMap-Notification-Service is configured.
     */
    public boolean isConfigured() {
        return !this.apiGatewayConfig.getOpenstreetmapNotificationServiceUrl().equals("unknown");
    }

    /**
     * Send Quality-Hub result to OpenStreetMap-Notification-Service.
     */
    public Mono<Void> sendQualityHubResult(QualityHubResultDto qualityHubResult) {
        Mono<Void> sendNotifications = invalidResults(qualityHubResult)
                .flatMap(this::toNotifications)
                .concatMap(this::sendNotification)
                .then();

        return deleteNotificationGroup(qualityHubResult.changesetId()).then(sendNotifications);
    }

    /**
     * Get notification group description.
     */
    private String getNotificationGroup(Long changesetId) {
        return NOTIFICATION_GROUP_PREFIX + changesetId;
    }

    /**
     * Get invalid results.
     */
    private Flux<QualityServiceResultDto> invalidResults(QualityHubResultDto hubResult) {
        return Flux.fromIterable(hubResult.qualityServiceResults())
                .filter(result -> !result.isValid());
    }

    /**
     * Create notifications for errors.
     */
    private Flux<NewNotification> toNotifications(QualityServiceResultDto result) {
        return Flux.fromIterable(result.errors())
                .map(err -> new NewNotification(
                        NotificationType.QUALITY_CHECK,
                        getNotificationGroup(result.changesetId()),
                        NOTIFICATION_COLOR,
                        result.qualityServiceId(),
                        result.qualityServiceId(),
                        result.qualityServiceId(),
                        err.errorText(),
                        err.errorGeometry()
                ));
    }

    /**
     * Send notifications to service.
     */
    private Mono<Void> sendNotification(NewNotification notification) {
        return webClient.post()
                .uri("/osm-notification-service/v1/notification/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(log::info)
                .doOnError(error -> log.error("Sending notification failed", error))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    /**
     * Delete notification group.
     */
    private Mono<Void> deleteNotificationGroup(Long changesetId) {
        String description = this.getNotificationGroup(changesetId);

        return webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/osm-notification-service/v1/group/delete")
                        .queryParam("description", description)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(log::info)
                .then();
        }
}
