package com.pe.fisi.sw.cooperApp.notifications.mapper;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class NotificationMapper {
    public NotificationEvent mapNotificationEvent(QueryDocumentSnapshot document) {
        Timestamp timestamp = document.getTimestamp("fechaCreacion");

        return NotificationEvent.builder()
                .idUsuario(document.getString("idUsuario"))
                .mensaje(document.getString("mensaje"))
                .tipo(document.getString("tipo"))
                .idCuenta(document.getString("idCuenta"))
                .idSolcitante(document.getString("idSolcitante"))
                .estado(document.getString("estado"))
                .fechaCreacion(timestamp != null ? timestamp.toDate().toInstant() : Instant.now())
                .build();
    }
}
