package com.pe.fisi.sw.cooperApp.notifications.service;

import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.notifications.codec.AccessCodeDecoder;
import com.pe.fisi.sw.cooperApp.notifications.codec.DecodedAccessCode;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationFactory;
import com.pe.fisi.sw.cooperApp.notifications.repository.NotificationRepository;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.security.service.FirebaseAuthService;
import com.pe.fisi.sw.cooperApp.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseAuthService firebaseAuthService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final NotificationFactory notificationFactory;
    private final AccessCodeDecoder accessCodeDecoder;
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
                        String mensaje = "Has sido invitado a unirte a la cuenta " + nombreCuenta +
                                " por el usuario " + nombreInvitador;

                        NotificationEvent event = notificationFactory.createAccountInvitation(
                                accountId, invitedUserId, inviterId, mensaje
                        );

                        return notificationRepository.saveNotification(event);
                    });
                });
    }

    @Override
    public Flux<NotificationEvent> getNotifications(String idUsuario) {
        return notificationRepository.getNotifications(idUsuario);
    }

    @Override
    public Mono<Void> requestAccess(String base64Code, String requesterUid) {
        DecodedAccessCode decoded;
        try {
            decoded = accessCodeDecoder.decode(base64Code);
        } catch (CustomException e) {
            return Mono.error(e);
        }

        if (System.currentTimeMillis() > decoded.expiration()) {
            return Mono.error(new CustomException(HttpStatus.GONE, "El código ha expirado"));
        }

        return firebaseAuthService.getUidByEmail(decoded.ownerEmail())
                .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .zipWith(firebaseAuthService.getEmailByUid(requesterUid))
                .flatMap(tuple -> {
                    String ownerUid = tuple.getT1();
                    String requesterEmail = tuple.getT2();

                    return Mono.zip(
                            userRepository.getNombreCompleto(requesterEmail),
                            accountRepository.getNombreCuenta(decoded.cuentaId())
                    ).flatMap(data -> {
                        String nombreSolicitante = data.getT1();
                        String nombreCuenta = data.getT2();

                        String mensaje = "El usuario " + nombreSolicitante + " solicita acceso a la cuenta " + nombreCuenta;

                        NotificationEvent event = notificationFactory.createAccessRequest(
                                decoded.cuentaId(), requesterUid, ownerUid, mensaje
                        );

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

    @Override
    public Mono<NotificationEvent> notifyAccountReport(String cuentaId, String reporterId, String ownerUid, String motivo, String urlsConcatenadas) {
        String mensaje = "Motivo del reporte: " + motivo + "\nArchivos adjuntos:\n" + urlsConcatenadas;

        NotificationEvent notification = notificationFactory.createAccountReport(
                cuentaId, reporterId, ownerUid, mensaje
        );

        return notificationRepository.saveNotification(notification).thenReturn(notification);
    }

    @Override
    public Mono<Void> notifyDeposit(String cuentaId, String usuarioUid, float monto) {
        String mensaje = "Se realizó un depósito de S/ " + monto + " en la cuenta.";
        NotificationEvent notification = notificationFactory.createDepositNotification(
                cuentaId, usuarioUid, monto, mensaje
        );
        return notificationRepository.saveNotification(notification);
    }

    @Override
    public Mono<Void> notifyWithdrawal(String cuentaId, String usuarioUid, float monto) {
        String mensaje = "Se realizó un retiro de S/ " + monto + " de la cuenta.";
        NotificationEvent notification = notificationFactory.createWithdrawNotification(
                cuentaId, usuarioUid, monto, mensaje
        );
        return notificationRepository.saveNotification(notification);
    }

    @Override
    public Mono<Void> notifyTransfer(String cuentaOrigenId, String cuentaDestinoId, String usuarioUid, float monto) {
        String mensajeOrigen = "Se transfirió S/ " + monto + " desde esta cuenta hacia la cuenta " + cuentaDestinoId;
        String mensajeDestino = "Se recibió una transferencia de S/ " + monto + " desde la cuenta " + cuentaOrigenId;

        NotificationEvent notificacionOrigen = notificationFactory.createTransferNotification(
                cuentaOrigenId, usuarioUid, monto, mensajeOrigen
        );
        NotificationEvent notificacionDestino = notificationFactory.createTransferNotification(
                cuentaDestinoId, usuarioUid, monto, mensajeDestino
        );

        return Mono.when(
                notificationRepository.saveNotification(notificacionOrigen),
                notificationRepository.saveNotification(notificacionDestino)
        );
    }

}
