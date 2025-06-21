package com.pe.fisi.sw.cooperApp.banking.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.pe.fisi.sw.cooperApp.banking.model.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.mapper.AccountResponseMapper;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccountRepository {
    @Autowired
    Firestore firestore;
    private static final String ACCOUNTS = "Accounts";
    private final AccountResponseMapper mapper;
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

    public Mono<String> getOwnerUidOfAccount(String cuentaId) {
        return Mono.fromCallable(() -> {
            CollectionReference accounts = firestore.collection(ACCOUNTS);
            ApiFuture<QuerySnapshot> future = accounts.whereEqualTo("cuentaId", cuentaId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Cuenta no encontrada con ID: " + cuentaId);
            }

            Account account = documents.get(0).toObject(Account.class);
            if (account.getCreador() == null || account.getCreador().getUid() == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Cuenta sin creador válido.");
            }

            return account.getCreador().getUid();
        }).subscribeOn(Schedulers.boundedElastic());
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
    public Mono<Account> getAccountById(String cuentaId) {
        return Mono.fromCallable(() -> {
            CollectionReference accounts = firestore.collection(ACCOUNTS);
            ApiFuture<QuerySnapshot> future = accounts.whereEqualTo("cuentaId", cuentaId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Cuenta no encontrada con ID: " + cuentaId);
            }
            return documents.get(0).toObject(Account.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }
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

    public Mono<Void> addMemberToAccount(String accountId, AccountUserDto newMember) {
        return Mono.fromCallable(() -> {
            DocumentReference docRef = firestore.collection(ACCOUNTS).document(accountId);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Cuenta no encontrada con ID: " + accountId);
            }

            Account account = snapshot.toObject(Account.class);

            // Inicializar listas si son null
            if (account.getMiembrosUid() == null) {
                account.setMiembrosUid(new ArrayList<>());
            }
            if (account.getMiembros() == null) {
                account.setMiembros(new ArrayList<>());
            }

            // Verificar si ya es miembro y agregarlo si no lo es
            if (!account.getMiembrosUid().contains(newMember.getUid())) {
                account.getMiembrosUid().add(newMember.getUid());
            }

            boolean yaEsMiembro = account.getMiembros().stream()
                    .anyMatch(m -> m.getUid().equals(newMember.getUid()));
            if (!yaEsMiembro) {
                account.getMiembros().add(newMember);
            }

            // Actualizar la cuenta
            docRef.set(account).get();
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
    public Mono<Void> actualizarCuenta(Account account) {
        return Mono.fromCallable(() -> {
            firestore.collection("Accounts")
                    .document(account.getCuentaId())
                    .set(account).get();
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
