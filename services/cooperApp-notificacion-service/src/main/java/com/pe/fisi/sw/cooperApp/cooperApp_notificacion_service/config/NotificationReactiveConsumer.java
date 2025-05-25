package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.config;

import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationReactiveConsumer {

    private final NotificationService notificationService;
    private final KafkaProperties kafkaProperties; // Inject Spring Boot's KafkaProperties

    @PostConstruct
    public void startReactiveConsumer() {
        // Retrieve consumer properties from application.yml
        ReceiverOptions<String, NotificationEvent> receiverOptions =
                ReceiverOptions.<String, NotificationEvent>create(kafkaProperties.buildConsumerProperties())
                        .subscription(Collections.singleton("notifications"));

        KafkaReceiver.create(receiverOptions)
                .receive()
                .doOnNext(record -> {
                    NotificationEvent event = record.value();
                    log.info("🔔 (REACTIVO) Evento recibido: {}", event);
                    notificationService.saveNotification(event).subscribe(); // Llamar al servicio
                })
                .subscribe();
    }
}