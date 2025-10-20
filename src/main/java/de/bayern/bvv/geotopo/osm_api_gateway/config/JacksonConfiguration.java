package de.bayern.bvv.geotopo.osm_api_gateway.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public JtsModule jtsModule() { return new JtsModule(); }


    @Bean
    public ObjectMapper objectMapper(JtsModule jtsModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(jtsModule);
        return objectMapper;
    }
}
