package com.pe.fisi.sw.cooperApp.banking.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.pe.fisi.sw.cooperApp.banking.dto.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.mapper.AccountMapper;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
    @Autowired
    Firestore firestore;
    private static final String ACCOUNTS = "Accounts";
    private final AccountMapper mapper;
    public Mono<AccountResponse> crearCuenta(Account cuenta) {
        return Mono.fromCallable(() -> {
                    var docRef = firestore.collection(ACCOUNTS).document();
                    cuenta.setCuentaId(docRef.getId()); // usa String para el ID
                    docRef.set(cuenta).get();
                    return AccountResponse.builder()
                            .nombreCuenta(cuenta.getNombreCuenta())
                            .tipo(cuenta.getTipo())
                            .estado(cuenta.getEstado())
                            .moneda(cuenta.getMoneda())
                            .saldo(cuenta.getSaldo())
                            .descripcion(cuenta.getDescripcion())
                            .creadorNombre(cuenta.getCreador().getFullName())
                            .creadorUid(cuenta.getCreador().getUid())
                            .fechaCreacion(cuenta.getFechaCreacion())
                            .build();
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.error(new CustomException(
                        HttpStatus.BAD_REQUEST, "Error al guardar en firestore: " + e.getMessage())));
    }
    public Flux<AccountResponse> getAllAccountsOwnerOfByUuid(String uuid) {
        return Mono.fromCallable(() -> {
                    CollectionReference accounts = firestore.collection(ACCOUNTS);
                    ApiFuture<QuerySnapshot> future = accounts.whereEqualTo("creador.uid", uuid).get(); // asegurarse que sea 'uid'
                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                    return documents.stream()
                            .map(doc -> doc.toObject(Account.class))
                            .map(mapper::toResponse)
                            .toList();
                }).flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<AccountResponse> getAllAccountsMemberOfByUuid(String uuid) {
        return Mono.fromCallable(() -> {
                    CollectionReference accounts = firestore.collection(ACCOUNTS);
                    ApiFuture<QuerySnapshot> future = accounts.whereArrayContains("miembrosUid", uuid).get();
                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                    return documents.stream()
                            .map(doc -> doc.toObject(Account.class))
                            .map(mapper::toResponse)
                            .toList();
                }).flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<AccountUserDto> getAllMembersOfByAccountId(String accountId) {
        return Mono.fromCallable(()->{
            CollectionReference accounts = firestore.collection(ACCOUNTS);
            ApiFuture<QuerySnapshot> future = accounts.whereEqualTo("cuentaId", accountId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                return List.<AccountUserDto>of(); // Devuelve una lista vacía si no encuentra nada
            }
            Account account = documents.get(0).toObject(Account.class);
            return account.getMiembros();
        }).flatMapMany(Flux::fromIterable).subscribeOn(Schedulers.boundedElastic());
    }
}
