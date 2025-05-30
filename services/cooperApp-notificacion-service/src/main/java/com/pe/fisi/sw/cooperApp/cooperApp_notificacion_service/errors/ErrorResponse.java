package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorResponse {
    private int status;
    private String message;
}
