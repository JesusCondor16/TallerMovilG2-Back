package com.pe.fisi.sw.cooperApp.cooperApp_gateway.security.jwt;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.pe.fisi.sw.cooperApp.cooperApp_gateway.security.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    public static FirebaseToken verifyToken(String token) throws FirebaseAuthException {
        try {
            return FirebaseAuth.getInstance(FirebaseApp.getInstance("gateway-app")).verifyIdToken(token);
        } catch (FirebaseAuthException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST,"Token inválido: " + e.getMessage());
        }
    }
}
