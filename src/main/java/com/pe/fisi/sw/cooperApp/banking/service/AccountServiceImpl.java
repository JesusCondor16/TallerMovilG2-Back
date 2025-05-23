package com.pe.fisi.sw.cooperApp.banking.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.banking.dto.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final UserService userService;
    private final Firestore firestore;

    @Override
    public Mono<AccountResponse> createAccount(CreateAccountRequest request) {
        return userService.findByUid(request.getCreadorUid())
                .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND,"Hubo un error no se encontro al creador de la cuenta")))
                .flatMap(creador ->{
                    log.info("Creador de la cuenta"+ creador.getUid());
                    AccountUserDto owner = AccountUserDto.builder()
                            .uid(creador.getUid())
                            .fullName(creador.getNombre()+" "+ creador.getApellido())
                            .dni(creador.getDni())
                            .tipoDocumento(creador.getTipoDocumento())
                            .email(creador.getEmail())
                            .build();
                    List<AccountUserDto> miembros = new ArrayList<>();
                    List<String> miembrosUid = new ArrayList<>();
                    miembrosUid.add(owner.getUid());
                    miembros.add(owner);
                    Account cuenta = Account.builder()
                            .nombreCuenta(request.getNombre())
                            .tipo(request.getTipo())
                            .estado("Activo")
                            .moneda(request.getMoneda())
                            .saldo(request.getSaldo())
                            .descripcion(request.getDescripcion())
                            .creador(owner)
                            .fechaCreacion(java.time.Instant.now())
                            .miembros(miembros)
                            .miembrosUid(miembrosUid)
                            .build();
                    return repository.crearCuenta(cuenta);
                });
    }

    @Override
    public Mono<List<AccountResponse>> getAllAcountsOwnerOfByUuid(String uuid) {
        return repository.getAllAccountsOwnerOfByUuid(uuid).collectList();
    }

    @Override
    public Mono<List<AccountResponse>> getAllAcountsMemberOfByUuid(String uuid) {
        return repository.getAllAccountsMemberOfByUuid(uuid).collectList();
    }

    @Override
    public Mono<List<AccountUserDto>> getAllMembersOfByAccountId(String accountId) {
        return repository.getAllMembersOfByAccountId(accountId).collectList();
    }

}
