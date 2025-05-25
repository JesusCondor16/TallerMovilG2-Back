package com.pe.fisi.sw.cooperApp.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "{validate.notblank.message}")
    private String refreshToken;
}
