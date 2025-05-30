package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class FirebaseAuthService {

    public Mono<String> getUidByEmail(String email) {
        return Mono.fromCallable(() -> {
            try {
                UserRecord user = FirebaseAuth.getInstance(FirebaseApp.getInstance("notifications-app"))
                        .getUserByEmail(email);
                return user.getUid();
            } catch (Exception e) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Usuario no encontrado con email: " + email);
            }
        });
    }
}
