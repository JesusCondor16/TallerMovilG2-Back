package com.pe.fisi.sw.cooperApp.users.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.pe.fisi.sw.cooperApp.users.dto.EditRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final Firestore firestore;
    public Mono<User> findById(String id) {
        return Mono.fromCallable(()->{
            DocumentSnapshot snapshot = firestore.collection("Users").document(id).get().get();
            return snapshot.toObject(User.class);
        }).subscribeOn(Schedulers.boundedElastic());

    }
    public Mono<User> edituser(EditRequest editRequest) {
        return Mono.fromCallable(()->{
            DocumentSnapshot snapshot = firestore.collection("Users").document(editRequest.getUid()).get().get();
            if(!snapshot.exists()) throw new CustomException(HttpStatus.BAD_REQUEST,"Usuario no existe en la base de datos");
            firestore.collection("Users")
                    .document(editRequest.getUid())
                    .update("email",editRequest.getEmail(),"telefono",editRequest.getTelefono()).get();
            DocumentSnapshot updatedSnapshot = firestore.collection("Users")
                    .document(editRequest.getUid())
                    .get()
                    .get();
            return updatedSnapshot.toObject(User.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<String> getNombreCompleto(String email) {
        return Mono.fromCallable(() -> {
            // Hacer consulta por el campo "email"
            QuerySnapshot querySnapshot = firestore.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .get(); // Bloqueante

            if (querySnapshot.isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Usuario no encontrado");
            }
            // Obtener el primer documento
            QueryDocumentSnapshot doc = querySnapshot.getDocuments().getFirst();
            User user = doc.toObject(User.class);
            return user.getNombre() + " " + user.getApellido();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
