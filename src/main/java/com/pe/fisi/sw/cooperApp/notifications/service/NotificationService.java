package com.pe.fisi.sw.cooperApp.notifications.service;

import com.pe.fisi.sw.cooperApp.notifications.dto.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> requestAccess(String base64Code, String requesterUid);
    Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId);
    Flux<NotificationEvent> getNotifications(String cuentaId);
}
