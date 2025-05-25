package com.pe.fisi.sw.cooperApp.banking.service;

import reactor.core.publisher.Mono;

public interface NotificationProdService {
    Mono<Void> requestAccess(String ownerEmail, String requesterId);
    Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId);
}
