package com.pe.fisi.sw.cooperApp.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "{validate.notblank.message}")
    @Email(message = "{validate.email.message}")
    private String email;

    @NotBlank(message = "{validate.notblank.message}")
    private String password;

    @NotBlank(message = "{validate.notblank.message}")
    private String firstname;

    @NotBlank(message = "{validate.notblank.message}")
    private String lastname;

    @NotBlank(message = "{validate.notblank.message}")
    private String tipoDocumento;

    private String fechaNacimiento;

    @NotBlank(message = "{validate.notblank.message}")
    @Pattern(regexp = "\\d{8}", message = "{validate.dni.size.message}")
    private String dni;

    @NotBlank(message = "{validate.notblank.message}")
    @Pattern(regexp = "\\d{9}", message = "{validate.phone.message}")
    private String telefono;

}

