package com.pe.fisi.sw.cooperApp.users.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.dto.EditRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final Firestore firestore;
    private static final String USERS = "Users";

    public Mono<Void> save(User user) {
        return Mono.fromCallable(() ->
                firestore.collection(USERS).document(user.getUid()).set(user).get()
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<User> findById(String id) {
        return Mono.fromCallable(() -> {
            DocumentSnapshot snapshot = firestore.collection(USERS).document(id).get().get();
            return snapshot.toObject(User.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<User> updateUser(EditRequest editRequest) {
        return Mono.fromCallable(() -> {
            DocumentSnapshot snapshot = firestore.collection(USERS).document(editRequest.getUid()).get().get();
            if (!snapshot.exists())
                throw new CustomException(HttpStatus.BAD_REQUEST, "Usuario no existe en la base de datos");

            firestore.collection(USERS)
                    .document(editRequest.getUid())
                    .update("email", editRequest.getEmail(), "telefono", editRequest.getTelefono())
                    .get();

            DocumentSnapshot updatedSnapshot = firestore.collection(USERS)
                    .document(editRequest.getUid())
                    .get()
                    .get();
            return updatedSnapshot.toObject(User.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> existsByDni(String dni) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("dni", dni)
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> existsByEmail(String email) {
        return Mono.fromCallable(() ->
                !firestore.collection(USERS)
                        .whereEqualTo("email", email)
                        .get()
                        .get()
                        .isEmpty()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getNombreCompleto(String email) {
        return Mono.fromCallable(() -> {
            QuerySnapshot querySnapshot = firestore.collection(USERS)
                    .whereEqualTo("email", email)
                    .get()
                    .get();
            if (querySnapshot.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
            }
            QueryDocumentSnapshot doc = querySnapshot.getDocuments().getFirst();
            User user = doc.toObject(User.class);
            return user.getNombre() + " " + user.getApellido();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AccountUserDto> getUserAsAccountDto(String userId) {
        return findById(userId)
                .map(user -> AccountUserDto.builder()
                        .uid(user.getUid())
                        .fullName(user.getNombre() + " " + user.getApellido())
                        .dni(user.getDni())
                        .email(user.getEmail())
                        .tipoDocumento(user.getTipoDocumento())
                        .build());
    }
}
