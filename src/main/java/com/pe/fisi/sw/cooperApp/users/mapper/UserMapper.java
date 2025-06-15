package com.pe.fisi.sw.cooperApp.users.mapper;

import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.users.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User fromRegisterRequest(String uid, RegisterRequest request) {
        LocalDate fechaNacimiento = LocalDateTime.parse(request.getFechaNacimiento()).toLocalDate();
        return User.builder()
                .uid(uid)
                .email(request.getEmail())
                .nombre(request.getFirstname())
                .apellido(request.getLastname())
                .fechaRegistro(Instant.now())
                .rol("cliente")
                .tipoDocumento(request.getTipoDocumento())
                .dni(request.getDni())
                .telefono(request.getTelefono())
                .fechaNacimiento(java.sql.Date.valueOf(fechaNacimiento))
                .estado("activo")
                .build();
    }
}
