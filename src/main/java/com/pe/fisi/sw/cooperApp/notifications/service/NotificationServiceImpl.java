package com.pe.fisi.sw.cooperApp.notifications.service;

import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.notifications.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.notifications.repository.NotificationRepository;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.security.service.FirebaseAuthService;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseAuthService firebaseAuthService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId) {
        log.info(accountId);
        return firebaseAuthService.getUidByEmail(email)
                .zipWith(firebaseAuthService.getEmailByUid(inviterId))
                .flatMap(tuple -> {
                    String invitedUserId = tuple.getT1();
                    String inviterEmail = tuple.getT2();

                    return Mono.zip(
                            userRepository.getNombreCompleto(inviterEmail),
                            accountRepository.getNombreCuenta(accountId)
                    ).flatMap(data -> {
                        String nombreInvitador = data.getT1();
                        String nombreCuenta = data.getT2();

                        NotificationEvent event = NotificationEvent.builder()
                                .idUsuario(invitedUserId)
                                .mensaje("Has sido invitado a unirte a la cuenta " + nombreCuenta +
                                        " por el usuario " + nombreInvitador)
                                .tipo("INVITACION_ACCESO_CUENTA")
                                .idCuenta(accountId)
                                .idSolcitante(inviterId)
                                .estado("pending")
                                .fechaCreacion(Instant.now())
                                .build();

                        return notificationRepository.saveNotification(event);
                    });
                });
    }

    @Override
    public Flux<NotificationEvent> getNotifications(String accountId) {
        return notificationRepository.getNotifications(accountId);
    }

    @Override
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
                .zipWith(firebaseAuthService.getEmailByUid(requesterUid))
                .flatMap(tuple -> {
                    String ownerUid = tuple.getT1();
                    String requesterEmail = tuple.getT2();

                    return Mono.zip(
                            userRepository.getNombreCompleto(requesterEmail),
                            accountRepository.getNombreCuenta(cuentaId)
                    ).flatMap(data -> {
                        String nombreSolicitante = data.getT1();
                        String nombreCuenta = data.getT2();

                        NotificationEvent event = NotificationEvent.builder()
                                .idUsuario(ownerUid)
                                .idSolcitante(requesterUid)
                                .idCuenta(cuentaId)
                                .estado("pending")
                                .tipo("SOLICITUD_ACCESO_CUENTA")
                                .fechaCreacion(Instant.now())
                                .mensaje("El usuario " + nombreSolicitante + " solicita acceso a la cuenta " + nombreCuenta)
                                .build();

                        return notificationRepository.saveNotification(event);
                    });
                });
    }
    @Override
    public Mono<String> acceptMember(String idNotificacion) {
        return notificationRepository.findById(idNotificacion)
                .flatMap(notification -> {
                    String idCuenta = notification.getIdCuenta();
                    String idSolicitante = notification.getIdSolcitante();

                    return userRepository.getUserAsAccountDto(idSolicitante)
                            .flatMap(accountUserDto -> {
                                // Ejecutar las operaciones en paralelo
                                Mono<Void> updateNotification = notificationRepository
                                        .updateNotificationStatus(idNotificacion, "aceptada");

                                Mono<Void> addMember = accountRepository
                                        .addMemberToAccount(idCuenta, accountUserDto);

                                return Mono.when(updateNotification, addMember)
                                        .thenReturn("Miembro aceptado y agregado correctamente.");
                            });
                });
    }

    @Override
    public Mono<String> rejectMember(String idNotificacion) {
        return notificationRepository.updateNotificationStatus(idNotificacion, "rechazada")
                .thenReturn("Solicitud de membresía rechazada correctamente.");
    }
}
