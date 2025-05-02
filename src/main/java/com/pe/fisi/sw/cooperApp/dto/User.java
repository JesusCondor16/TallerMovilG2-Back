package com.pe.fisi.sw.cooperApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
@Data
@Builder
@AllArgsConstructor
public class User {
    private String uid;
    private String email;
    private String nombre;
    private String apellido;
    private String rol;
    private Instant fechaRegistro;
}
