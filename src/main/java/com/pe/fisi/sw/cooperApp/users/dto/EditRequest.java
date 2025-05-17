package com.pe.fisi.sw.cooperApp.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EditRequest {
    @NotBlank(message = "{validate.notblank.message}")
    @Email(message = "{validate.email.message}")
    public String email;
    @NotBlank(message = "{validate.notblank.message}")
    public String telefono;
    @NotBlank(message = "{validate.notblank.message}")
    public String uid;
}
