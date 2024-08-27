package com.idirtrack.backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.idirtrack.backend.traccar.TracCarUser;



@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public TracCarUser tracCarUser() {
        return new TracCarUser();
    }


    
}