package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.repository;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationRepository {
    private final NotificationMapper notificationMapper;
    @Autowired
    Firestore firestore;
    private static final String NOTIFICATIONS = "Notifications";
    public Flux<NotificationEvent> getNotifications(String accountId) {
        return Mono.fromFuture(
                CompletableFuture.supplyAsync(()->{
                    try{
                        return firestore.collection(NOTIFICATIONS).whereEqualTo("idCuenta", accountId).get().get().getDocuments();
                    } catch (Exception e){
                        throw new CustomException(HttpStatus.BAD_REQUEST, "Error al obtener las notificaciones"+ e.getMessage());
                    }
                })
        ).flatMapMany(Flux::fromIterable).map(notificationMapper::mapNotificationEvent);
    }
}
