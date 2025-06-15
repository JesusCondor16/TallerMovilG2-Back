package com.pe.fisi.sw.cooperApp.notifications.codec;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class Base64AccessCodeDecoder implements AccessCodeDecoder {
    @Override
    public DecodedAccessCode decode(String base64Code) throws CustomException {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64Code), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Código inválido");
        }

        String[] parts = decoded.split(":");
        if (parts.length != 3) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Formato del código incorrecto");
        }

        String cuentaId = parts[0];
        long expiration;
        try {
            expiration = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Timestamp inválido");
        }

        String ownerEmail = parts[2];

        return new DecodedAccessCode(cuentaId, expiration, ownerEmail);
    }
}
