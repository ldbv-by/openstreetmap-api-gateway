package de.bayern.bvv.geotopo.osm_api_gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public JtsJackson3Module jtsJackson3Module() {
        return new JtsJackson3Module();
    }
}
