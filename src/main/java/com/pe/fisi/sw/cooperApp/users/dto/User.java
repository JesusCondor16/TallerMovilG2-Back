package com.pe.fisi.sw.cooperApp.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String rol;
    private String fotoUrl;
    private Instant fechaRegistro;
    private String tipoDocumento;
    private String dni;
    private String estado;

}
