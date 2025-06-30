package com.pe.fisi.sw.cooperApp.users.mapper;

import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.model.User;
import org.springframework.stereotype.Service;

@Service
public class AccountUserMapper {
    public AccountUserDto toAccountUserDto(User user) {
        return AccountUserDto.builder()
                .uid(user.getUid())
                .fullName(user.getNombre() + " " + user.getApellido())
                .dni(user.getDni())
                .tipoDocumento(user.getTipoDocumento())
                .email(user.getEmail())
                .build();
    }
}
