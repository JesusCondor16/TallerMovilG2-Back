package com.pe.fisi.sw.cooperApp.notifications.service;

import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> requestAccess(String base64Code, String requesterUid);
    Mono<Void> inviteUserToAccount(String email, String accountId, String inviterId);
    Flux<NotificationEvent> getNotifications(String idUsuario);

    Mono<String> acceptMember(String idNotificacion);

    Mono<String> rejectMember(String idNotificacion);
    Mono<NotificationEvent> notifyAccountReport(String cuentaId, String reporterId, String ownerUid, String motivo, String urlsConcatenadas);
    Mono<Void> notifyDeposit(String cuentaId, String usuarioUid, float monto);
    Mono<Void> notifyWithdrawal(String cuentaId, String usuarioUid, float monto);
    Mono<Void> notifyTransfer(String cuentaOrigenId, String cuentaDestinoId, String usuarioUid, float monto);
}
