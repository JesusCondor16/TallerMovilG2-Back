package com.pe.fisi.sw.cooperApp.banking.mapper;

import com.pe.fisi.sw.cooperApp.banking.dto.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import org.springframework.stereotype.Service;

@Service
public class AccountMapper {
    public AccountResponse toResponse(Account cuenta) {
        return AccountResponse.builder()
                .nombreCuenta(cuenta.getNombreCuenta())
                .tipo(cuenta.getTipo())
                .estado(cuenta.getEstado())
                .moneda(cuenta.getMoneda())
                .saldo(cuenta.getSaldo())
                .descripcion(cuenta.getDescripcion())
                .creadorNombre(cuenta.getCreador().getFullName())
                .creadorUid(cuenta.getCreador().getUid())
                .fechaCreacion(cuenta.getFechaCreacion())
                .build();
    }
}