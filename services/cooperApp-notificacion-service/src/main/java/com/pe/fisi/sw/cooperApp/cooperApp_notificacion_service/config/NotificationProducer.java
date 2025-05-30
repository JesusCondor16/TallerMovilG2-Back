package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.config;

import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final ReactiveKafkaProducerTemplate<String, NotificationEvent> kafkaProducerTemplate;

    private static final String TOPIC = "notifications";

    public Mono<Void> sendNotificationEvent(NotificationEvent event) {
        log.info("Enviando evento de notificación: {}", event);
        return kafkaProducerTemplate.send(TOPIC, event.getIdUsuario(), event)
                .doOnSuccess(result -> log.info("Notificación enviada correctamente a Kafka: {}", result))
                .doOnError(error -> log.error("Error al enviar evento a Kafka", error))
                .then();
    }
}
