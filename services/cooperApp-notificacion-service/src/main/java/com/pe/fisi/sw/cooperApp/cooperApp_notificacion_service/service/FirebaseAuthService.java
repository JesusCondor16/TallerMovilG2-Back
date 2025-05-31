package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


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
    public Mono<String> getEmailByUid(String uid) {
        return Mono.fromCallable(() -> {
            UserRecord userRecord = FirebaseAuth.getInstance(FirebaseApp.getInstance("notifications-app")).getUser(uid);
            return userRecord.getEmail();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
