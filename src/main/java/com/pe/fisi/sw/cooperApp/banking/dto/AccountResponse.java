package com.pe.fisi.sw.cooperApp.banking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AccountResponse {
    private String cuentaId;
    private String nombreCuenta;
    private String tipo;
    private String estado;
    private String moneda;
    private float saldo;
    private String descripcion;
    private String creadorNombre;
    private String creadorUid;
    private Instant fechaCreacion;
}
