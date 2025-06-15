package com.pe.fisi.sw.cooperApp.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String idNotification;
    private String idUsuario; //id del usuario que recibe la notificacion
    private String mensaje;
    private String tipo;
    private String idCuenta;  // id de la cuenta a ser invitado
    private String idSolcitante; // id genero la solcitud
    private String estado;
    private Instant fechaCreacion;
    private Instant fechaModificacion;
}