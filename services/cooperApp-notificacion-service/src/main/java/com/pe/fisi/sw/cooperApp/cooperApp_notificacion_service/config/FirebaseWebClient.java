package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FirebaseWebClient {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
