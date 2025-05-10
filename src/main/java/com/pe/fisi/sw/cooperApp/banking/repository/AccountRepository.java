package com.pe.fisi.sw.cooperApp.banking.repository;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.banking.dto.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
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
                            .creadorNombre(cuenta.getCreador().getNombre())
                            .creadorUid(cuenta.getCreador().getUid())
                            .fechaCreacion(cuenta.getFechaCreacion())
                            .build();
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.error(new CustomException(
                        HttpStatus.BAD_REQUEST, "Error al guardar en firestore: " + e.getMessage())));
    }
}
