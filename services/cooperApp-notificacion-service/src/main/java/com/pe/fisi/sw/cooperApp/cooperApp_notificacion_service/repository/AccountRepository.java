package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.dto.Account;
import com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
    @Autowired
    Firestore firestore;
    private static final String ACCOUNTS = "Accounts";
    public Mono<String> getNombreCuenta(String cuentaId) {
        return Mono.fromCallable(() -> {
            DocumentSnapshot snapshot = firestore.collection(ACCOUNTS).document(cuentaId).get().get();
            Account account = snapshot.toObject(Account.class);
            if (account != null) {
                return account.getNombreCuenta();
            } else {
                throw new CustomException(HttpStatus.BAD_REQUEST,"Cuenta no encontrada");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
