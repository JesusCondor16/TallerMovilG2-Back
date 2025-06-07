package com.pe.fisi.sw.cooperApp.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportRequest {
    private String cuentaUid;
    private String reporterId;
    private String motivo;
}
