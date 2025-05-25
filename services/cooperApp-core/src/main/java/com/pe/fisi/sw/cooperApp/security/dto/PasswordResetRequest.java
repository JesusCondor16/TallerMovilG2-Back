package com.pe.fisi.sw.cooperApp.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "{validate.notblank.message}")
    @Email(message = "{validate.email.message}")
    private String email;
}
