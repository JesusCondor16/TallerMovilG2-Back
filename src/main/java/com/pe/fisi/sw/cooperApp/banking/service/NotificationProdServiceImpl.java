package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.config.NotificationProducer;
import com.pe.fisi.sw.cooperApp.banking.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.security.service.FirebaseAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class NotificationProdServiceImpl implements NotificationProdService {
    private final FirebaseAuthService firebaseAuthService;
    private final NotificationProducer kafkaProducer;
    public Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId) {
        // 1. Buscar UID del usuario invitado usando Firebase Auth
        return firebaseAuthService.getUidByEmail(email)
                .flatMap(invitedUserId -> {
                    // 2. Crear evento de notificación
                    NotificationEvent event = NotificationEvent.builder()
                            .userId(invitedUserId)   // usuario que recibirá la notificación
                            .message("Has sido invitado a unirte a la cuenta " + accountId + " por el usuario " + inviterId)
                            .type("account-invite")
                            .referenceId(accountId)
                            .status("pending")
                            .build();
                    // 3. Enviar a Kafka
                    return kafkaProducer.sendNotificationEvent(event);
                });
    }
    public Mono<Void> requestAccess(String ownerEmail, String requesterId) {
        // 1. Obtener UID del dueño de cuenta a partir del email
        return firebaseAuthService.getUidByEmail(ownerEmail)
                .flatMap(ownerUid -> {
                    // 2. Crear evento para Kafka
                    NotificationEvent event = NotificationEvent.builder()
                            .userId(ownerUid)                   // dueño de cuenta
                            .message("El usuario " + requesterId + " quiere unirse a tu cuenta.")
                            .type("account-join-request")
                            .referenceId(requesterId)
                            .status("pending")
                            .build();
                    // 3. Enviar a Kafka
                    return kafkaProducer.sendNotificationEvent(event);
                });
    }
}
