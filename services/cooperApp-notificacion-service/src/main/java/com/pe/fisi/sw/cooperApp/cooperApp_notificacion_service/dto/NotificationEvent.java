package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String userId;
    private String message;
    private String type;
    private String referenceId;
    private String status;
}

