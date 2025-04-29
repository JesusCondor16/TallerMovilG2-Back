package com.pe.fisi.sw.cooperApp.security.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    @Autowired
    Firestore firestore;
    private static final String USERS = "Users";

    public Mono<Void> createUserFirestore(String uid, RegisterRequest request) {
        Map<String, Object> userData = Map.of(
                "uid", uid,
                "email", request.getEmail(),
                "nombre", request.getFirstname(),
                "apellido", request.getLastname(),
                "rol", "cliente",
                "fechaRegistro", Instant.now()
        );

        return Mono.fromCallable(() ->
                        firestore.collection(USERS).document(uid).set(userData).get()
                )
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .onErrorResume(e -> Mono.error(new RuntimeException("Error al guardar en Firestore: " + e.getMessage())));
    }
}
