package com.pe.fisi.sw.cooperApp.notifications.codec;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;

public interface AccessCodeDecoder {
    DecodedAccessCode decode(String base64Code) throws CustomException;
}
