package com.pe.fisi.sw.cooperApp.notifications.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j

public class IAService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://deploy-iaservice-ccdbaf24365b.herokuapp.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public Mono<IAResponse> analizarComportamiento(InputIA input) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(IAResponse.class);
    }

    @Data
    public static class InputIA {
        private int frecuenciaMesActual;
        private int frecuenciaMesAnterior;
        private float montoPromedioActual;
        private float montoPromedioAnterior;
        private int incumplimientosTotales;
        private int edad;
        private int variacionFrecuencia;
    }

    @Data
    public static class IAResponse {
        private int disminuyo;
        private float probabilidad;
    }
}
