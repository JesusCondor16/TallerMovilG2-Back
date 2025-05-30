package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;


import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.config.NotificationProducer;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

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
                            .idUsuario(invitedUserId)   // usuario que recibirá la notificación
                            .mensaje("Has sido invitado a unirte a la cuenta " + accountId + " por el usuario " + inviterId)
                            .tipo("INVITACION_ACCESO_CUENTA")
                            .idCuenta(accountId)
                            .idSolcitante(inviterId)
                            .estado("pending")
                            .build();
                    // 3. Enviar a Kafka
                    return kafkaProducer.sendNotificationEvent(event);
                });
    }
    public Mono<Void> requestAccess(String base64Code, String requesterUid) {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64Code), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Código inválido"));
        }

        String[] parts = decoded.split(":");
        if (parts.length != 3) {
            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Formato del código incorrecto"));
        }

        String cuentaId = parts[0];
        long expiration;
        String ownerEmail = parts[2];

        try {
            expiration = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Timestamp inválido"));
        }

        if (System.currentTimeMillis() > expiration) {
            return Mono.error(new CustomException(HttpStatus.GONE, "El código ha expirado"));
        }

        return firebaseAuthService.getUidByEmail(ownerEmail)
                .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .flatMap(ownerUid -> {
                    NotificationEvent event = NotificationEvent.builder()
                            .idUsuario(ownerUid)
                            .idSolcitante(requesterUid)
                            .idCuenta(cuentaId)
                            .estado("pending")
                            .tipo("SOLICITUD_ACCESO_CUENTA")
                            .fechaCreacion(Instant.now())
                            .mensaje("El usuario " + requesterUid + " solicita acceso a la cuenta " + cuentaId)
                            .build();
                    return kafkaProducer.sendNotificationEvent(event);
                });
    }

}
