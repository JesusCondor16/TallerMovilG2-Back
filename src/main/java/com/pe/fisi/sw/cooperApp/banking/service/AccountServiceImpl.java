package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.mapper.AccountResponseMapper;
import com.pe.fisi.sw.cooperApp.banking.model.AccountFactory;
import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.notifications.codec.AccessCodeEncoder;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.mapper.AccountUserMapper;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final UserService userService;
    private final AccountResponseMapper accountResponseMapper;
    private final AccountFactory accountFactory;
    private final AccountUserMapper accountUserMapper;
    private final AccessCodeEncoder accessCodeEncoder;
    @Override
    public Mono<AccountResponse> createAccount(CreateAccountRequest request) {
        return userService.findByUid(request.getCreadorUid())
                .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND,"Hubo un error no se encontro al creador de la cuenta")))
                .map(accountUserMapper::toAccountUserDto)
                .map(owner -> accountFactory.create(request,owner))
                .flatMap(repository::crearCuenta);
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

    @Override
    public Mono<String> generateCode(String cuentaId) {
        long expiration = System.currentTimeMillis() + 3600_000;

        return repository.getAccountById(cuentaId)
                .map(account -> accessCodeEncoder.encode(
                        cuentaId,
                        expiration,
                        account.getCreador().getEmail()
                ));
    }

    @Override
    public Mono<AccountResponse> getAccountDetails(String cuentauid) {
        return repository.getAccountById(cuentauid).map(accountResponseMapper::toResponse);
    }

}
