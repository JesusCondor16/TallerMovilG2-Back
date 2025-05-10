package com.pe.fisi.sw.cooperApp.users.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.users.dto.User;
import lombok.RequiredArgsConstructor;
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
}
