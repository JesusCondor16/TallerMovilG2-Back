package com.pe.fisi.sw.cooperApp.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class AccountUserDto {
    private String uid;
    private String fullName;
    private String dni;
    private String tipoDocumento;
    private String email;
}
