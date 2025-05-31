package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;

import com.google.common.io.Files;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationProdService {
    Mono<Void> requestAccess(String base64Code, String requesterUid);
    Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId);

    Flux<NotificationEvent> getNotifications(String cuentaId);
}
