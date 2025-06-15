package com.pe.fisi.sw.cooperApp.banking.model;

import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC)
@AllArgsConstructor
public class Account {
    private String cuentaId;
    private String nombreCuenta;
    private String tipo;
    private String estado;
    private String moneda;
    private float saldo;
    private String descripcion;
    private AccountUserDto creador;
    private Instant fechaCreacion;
    private List<AccountUserDto> miembros;
    private List<String> miembrosUid;
}

