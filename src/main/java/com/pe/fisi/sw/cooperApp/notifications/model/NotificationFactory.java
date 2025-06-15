package com.pe.fisi.sw.cooperApp.notifications.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationFactory {
    public NotificationEvent createAccountInvitation(String cuentaId, String invitedUid, String inviterUid, String mensaje) {
        return NotificationEvent.builder()
                .idNotification(UUID.randomUUID().toString())
                .idCuenta(cuentaId)
                .idUsuario(invitedUid)
                .idSolcitante(inviterUid)
                .mensaje(mensaje)
                .estado("pending")
                .tipo(NotificationType.INVITACION_ACCESO_CUENTA.name())
                .fechaCreacion(Instant.now())
                .build();
    }

    public NotificationEvent createAccountReport(String cuentaId, String reporterId, String ownerUid, String mensaje) {
        return NotificationEvent.builder()
                .idNotification(UUID.randomUUID().toString())
                .idCuenta(cuentaId)
                .idUsuario(ownerUid)
                .idSolcitante(reporterId)
                .mensaje(mensaje)
                .estado("pending")
                .tipo(NotificationType.REPORTE_CUENTA.name())
                .fechaCreacion(Instant.now())
                .fechaModificacion(Instant.now())
                .build();
    }
    public NotificationEvent createAccessRequest(String cuentaId, String requesterUid, String ownerUid, String mensaje) {
        return NotificationEvent.builder()
                .idNotification(UUID.randomUUID().toString())
                .idCuenta(cuentaId)
                .idUsuario(ownerUid)
                .idSolcitante(requesterUid)
                .mensaje(mensaje)
                .estado("pending")
                .tipo(NotificationType.SOLICITUD_ACCESO_CUENTA.name())
                .fechaCreacion(Instant.now())
                .build();
    }

}
