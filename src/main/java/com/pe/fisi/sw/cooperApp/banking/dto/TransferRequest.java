package com.pe.fisi.sw.cooperApp.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotBlank
    private String cuentaOrigenId;
    @NotBlank
    private String cuentaDestinoId;
    @NotBlank
    private String usuarioUid;
    @Positive
    private float monto;
}
