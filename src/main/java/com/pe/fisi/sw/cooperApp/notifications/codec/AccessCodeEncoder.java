package com.pe.fisi.sw.cooperApp.notifications.codec;

public interface AccessCodeEncoder {
    String encode(String cuentaId, long expiration, String email);
}
