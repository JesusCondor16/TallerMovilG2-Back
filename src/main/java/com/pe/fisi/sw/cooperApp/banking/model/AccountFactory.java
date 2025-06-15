package com.pe.fisi.sw.cooperApp.banking.model;

import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class AccountFactory {
    public Account create(CreateAccountRequest request, AccountUserDto creator) {
        List<AccountUserDto> miembros = List.of(creator);
        List<String> miembrosUid = List.of(creator.getUid());
        return Account.builder()
                .nombreCuenta(request.getNombre())
                .tipo(request.getTipo())
                .estado("Activo")
                .moneda(request.getMoneda())
                .saldo(request.getSaldo())
                .descripcion(request.getDescripcion())
                .creador(creator)
                .fechaCreacion(Instant.now())
                .miembros(miembros)
                .miembrosUid(miembrosUid)
                .build();
    }
}
