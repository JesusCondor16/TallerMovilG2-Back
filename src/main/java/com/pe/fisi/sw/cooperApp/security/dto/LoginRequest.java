package com.pe.fisi.sw.cooperApp.security.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Email(message = "{validate.email.message}")
    @NotBlank(message = "{validate.notblank.message}")
    private String email;
    @NotBlank(message = "{validate.notblank.message}")
    private String password;
}
