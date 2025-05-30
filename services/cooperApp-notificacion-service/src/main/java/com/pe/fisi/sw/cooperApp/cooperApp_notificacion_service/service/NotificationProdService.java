package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;

import reactor.core.publisher.Mono;

public interface NotificationProdService {
    Mono<Void> requestAccess(String base64Code, String requesterUid);
    Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId);
}
