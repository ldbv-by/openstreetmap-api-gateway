package de.bayern.bvv.geotopo.osm_api_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openstreetmap-api-gateway")
@Data
public class ApiGatewayConfig {
    private String openstreetmapQualityFrameworkUrl;
}
