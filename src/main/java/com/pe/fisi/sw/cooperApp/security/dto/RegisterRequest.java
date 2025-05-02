package com.pe.fisi.sw.cooperApp.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email(message = "{validate.email.message}")
    @NotBlank(message = "{validate.notblank.message}")
    private String email;
    @NotBlank(message = "{validate.notblank.message}")
    private String password;
    @NotBlank(message = "{validate.notblank.message}")
    private String firstname;
    @NotBlank(message = "{validate.notblank.message}")
    private String lastname;
}