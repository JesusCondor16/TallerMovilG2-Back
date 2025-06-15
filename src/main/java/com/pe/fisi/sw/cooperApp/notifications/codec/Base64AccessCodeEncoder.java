package com.pe.fisi.sw.cooperApp.notifications.codec;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class Base64AccessCodeEncoder implements  AccessCodeEncoder {
    @Override
    public String encode(String cuentaId, long expiration, String email) {
        String raw = cuentaId + ":" + expiration + ":" + email;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
