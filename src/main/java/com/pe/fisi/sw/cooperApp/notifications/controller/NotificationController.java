package com.pe.fisi.sw.cooperApp.notifications.controller;

import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import com.pe.fisi.sw.cooperApp.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping("/get-all")
    public ResponseEntity<Flux<NotificationEvent>> getNotificaciones(
            @RequestParam String idUsuario
    ) {
        Flux<NotificationEvent> notifications = notificationService.getNotifications(idUsuario);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(notifications);
    }
    @PostMapping("/invite-member")
    public Mono<ResponseEntity<String>> inviteUserToAccount(
            @RequestParam String email,      // Email del invitado
            @RequestParam String cuentaId,  // ID de la cuenta
            @RequestParam String inviterUid   // Dueño de la cuenta
    ) {
        return notificationService.inviteUserToAccount(email, cuentaId, inviterUid)
                .thenReturn(ResponseEntity.ok("Invitación enviada"));
    }
    @PostMapping("/request-access")
    public Mono<ResponseEntity<String>> requestAccessToAccount(
            @RequestParam String code, //  en b64
            @RequestParam String requesterUid // <-- uid del que quiere unirse
    ) {
        return notificationService.requestAccess(code, requesterUid)
                .thenReturn(ResponseEntity.ok("Solicitud enviada"));
    }
    @PostMapping("/accept-member")
    public Mono<ResponseEntity<String>> acceptMemberToAccount(@RequestParam String idNotificacion){
        return notificationService.acceptMember(idNotificacion).thenReturn(ResponseEntity.ok("Solicitud aceptada"));
    }
    @PostMapping("/reject-member")
    public Mono<ResponseEntity<String>> rejectMemberToAccount(@RequestParam String idNotificacion){
        return notificationService.rejectMember(idNotificacion).thenReturn(ResponseEntity.ok("Solicitud rechazada"));
    }
}
