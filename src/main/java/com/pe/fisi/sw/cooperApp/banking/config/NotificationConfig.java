package com.pe.fisi.sw.cooperApp.banking.config;

import com.pe.fisi.sw.cooperApp.banking.dto.NotificationEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class NotificationConfig {

    private final KafkaProperties kafkaProperties; // Inject KafkaProperties

    public NotificationConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, NotificationEvent> reactiveKafkaProducerTemplate() {
        // Retrieve producer properties from application.yml
        SenderOptions<String, NotificationEvent> senderOptions = SenderOptions.create(kafkaProperties.buildProducerProperties());
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}