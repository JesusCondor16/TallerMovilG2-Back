package com.pe.fisi.sw.cooperApp.security.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.dto.User;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Service
public class UserService {
    @Autowired
    Firestore firestore;

    private static final String USERS = "Users";

    public Mono<Void> createUserFirestore(String uid, RegisterRequest request) {
        User user = User.builder()
                .uid(uid)
                .email(request.getEmail())
                .nombre(request.getFirstname())
                .apellido(request.getLastname())
                .fechaRegistro(Instant.now())
                .rol("cliente")
                .tipoDocumento(request.getTipoDocumento())
                .dni(request.getDni())
                .telefono(request.getTelefono())
                .username(request.getUsername())
                .build();

        return Mono.fromCallable(() ->
                        firestore.collection(USERS).document(uid).set(user).get()
                )
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .onErrorResume(e ->
                        Mono.error(new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Error al guardar el usuario en Firestore: " + e.getMessage()))
                );
    }
    public Mono<Boolean> validateDni(RegisterRequest request) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("dni", request.getDni())
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> validateEmail(RegisterRequest request) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("email", request.getEmail())
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }

}
