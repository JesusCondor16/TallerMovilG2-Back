package com.pe.fisi.sw.cooperApp.security.dto;

import lombok.Data;

@Data
public class PasswordResetConfirm {
    private String email;
    private String code;
    private String newPassword;
}
