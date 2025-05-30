package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.controller;

import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service.NotificationProdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationProdService notificationService;
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

}
