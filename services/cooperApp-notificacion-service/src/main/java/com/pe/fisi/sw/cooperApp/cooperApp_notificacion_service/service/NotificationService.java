package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final Firestore firestore;
    private static final String NOTIFICATIONS_COLLECTION = "Notifications";

    public Mono<Void> saveNotification(NotificationEvent event) {
        return Mono.fromCallable(() -> {
                    String documentId = firestore.collection(NOTIFICATIONS_COLLECTION).document().getId();
                    event.setReferenceId(documentId); // opcional: puedes usarlo como ID del doc
                    firestore.collection(NOTIFICATIONS_COLLECTION).document(documentId).set(event).get();
                    log.info("🔔 Notificación guardada en Firestore con ID: {}", documentId);
                    return true;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .onErrorResume(e -> {
                    log.error("❌ Error al guardar notificación: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error al guardar notificación", e));
                });
    }
}
