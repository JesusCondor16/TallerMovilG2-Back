package com.pe.fisi.sw.cooperApp.users.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.users.dto.EditRequest;
import com.pe.fisi.sw.cooperApp.users.dto.User;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
    Firestore firestore;
    private final UserRepository userRepository;
    private static final String USERS = "Users";

    public Mono<Void> createUserFirestore(String uid, RegisterRequest request) {
        LocalDate fechaNacimiento = LocalDateTime.parse(request.getFechaNacimiento()).toLocalDate();
        Date fechaNacimientoDate = java.sql.Date.valueOf(fechaNacimiento);
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
                .fechaNacimiento(
                        fechaNacimientoDate
                )
                .estado("activo")
                .build();
        log.info(request.getFechaNacimiento());
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
    public Mono<Boolean> validateDni(String dni) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("dni", dni)
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> validateEmail(String email) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("email", email)
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<User> findByUid(String uid) {
        return userRepository.findById(uid);
    }
    public Mono<User> editProfile(EditRequest editRequest){
        return userRepository.edituser(editRequest);
    }
}
