package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.repository;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.User;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
@Repository
@RequiredArgsConstructor
public class UserRepository {
    @Autowired
    Firestore firestore;
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
