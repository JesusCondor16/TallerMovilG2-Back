package com.pe.fisi.sw.cooperApp.notifications.repository;

import com.google.cloud.firestore.*;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import com.pe.fisi.sw.cooperApp.notifications.mapper.NotificationMapper;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationRepository {

    private final NotificationMapper notificationMapper;

    @Autowired
    private Firestore firestore;

    private static final String NOTIFICATIONS = "Notifications";

    public Mono<NotificationEvent> findById(String idNotificacion) {
        return Mono.fromCallable(() -> {
            DocumentSnapshot snapshot = firestore.collection(NOTIFICATIONS)
                    .document(idNotificacion)
                    .get()
                    .get(); // <- .get() bloqueante
            if (!snapshot.exists()) {
                throw new CustomException(HttpStatus.NOT_FOUND, "No se encontró notificación con ID: " + idNotificacion);
            }
            return snapshot.toObject(NotificationEvent.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<NotificationEvent> getNotifications(String accountId) {
        return Mono.fromFuture(
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return firestore.collection(NOTIFICATIONS)
                                        .whereEqualTo("idUsuario", accountId)
                                        .get().get().getDocuments();
                            } catch (Exception e) {
                                throw new CustomException(HttpStatus.BAD_REQUEST, "Error al obtener las notificaciones: " + e.getMessage());
                            }
                        })
                ).flatMapMany(Flux::fromIterable)
                .map(notificationMapper::mapNotificationEvent);
    }

    public Mono<Void> saveNotification(NotificationEvent event) {
        return Mono.fromCallable(() -> {
                    String documentId = firestore.collection(NOTIFICATIONS).document().getId();
                    event.setIdNotification(documentId); // Asigna el ID del documento a la notificación
                    firestore.collection(NOTIFICATIONS).document(documentId).set(event).get();
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
    public Mono<Void> updateNotificationStatus(String idNotificacion, String estado) {
        return Mono.fromCallable(() -> {
            DocumentReference docRef = firestore.collection(NOTIFICATIONS).document(idNotificacion);
            docRef.update(
                    "estado", estado,
                    "fechaModificacion", Instant.now()
            ).get();
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
