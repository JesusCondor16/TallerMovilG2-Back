package com.pe.fisi.sw.cooperApp.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "{validate.notblank.message}")
    private String nombre;

    @NotBlank(message = "{validate.notblank.message}")
    private String tipo;

    @NotBlank(message = "{validate.notblank.message}")
    private String moneda;

    @PositiveOrZero(message = "{validate.positiver.message}")
    private float saldo;

    private String descripcion;

    @NotBlank(message = "{validate.notblank.message}")
    private String creadorUid;
}
