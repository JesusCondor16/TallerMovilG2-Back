package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.*;
import com.pe.fisi.sw.cooperApp.banking.mapper.AccountResponseMapper;
import com.pe.fisi.sw.cooperApp.banking.model.AccountFactory;
import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.notifications.codec.AccessCodeEncoder;
import com.pe.fisi.sw.cooperApp.notifications.service.NotificationService;
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
    private final NotificationService notificationService;

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

    @Override
    public Mono<Void> deposit(DepositRequest request) {
        return repository.getAccountById(request.getCuentaId())
                .flatMap(account -> {
                    if (!account.getMiembrosUid().contains(request.getUsuarioUid())) {
                        return Mono.error(new CustomException(HttpStatus.FORBIDDEN, "Usuario no pertenece a la cuenta"));
                    }
                    account.setSaldo(account.getSaldo() + request.getMonto());
                    return repository.actualizarCuenta(account)
                            .then(notificationService.notifyDeposit(
                                    request.getCuentaId(),
                                    request.getUsuarioUid(),
                                    request.getMonto()
                            ));
                });
    }

    @Override
    public Mono<Void> withdraw(WithdrawRequest request) {
        return repository.getAccountById(request.getCuentaId())
                .flatMap(account -> {
                    if (!account.getMiembrosUid().contains(request.getUsuarioUid())) {
                        return Mono.error(new CustomException(HttpStatus.FORBIDDEN, "Usuario no pertenece a la cuenta"));
                    }
                    if (account.getSaldo() < request.getMonto()) {
                        return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Saldo insuficiente"));
                    }
                    account.setSaldo(account.getSaldo() - request.getMonto());
                    return repository.actualizarCuenta(account)
                            .then(notificationService.notifyWithdrawal(
                                    request.getCuentaId(),
                                    request.getUsuarioUid(),
                                    request.getMonto()
                            ));
                });
    }

    @Override
    public Mono<Void> invest(InvestmentRequest request) {
        return repository.getAccountById(request.getCuentaId())
                .flatMap(account -> {
                    if (!account.getMiembrosUid().contains(request.getUsuarioUid())) {
                        return Mono.error(new CustomException(HttpStatus.FORBIDDEN, "Usuario no pertenece a la cuenta"));
                    }
                    if (account.getSaldo() < request.getMonto()) {
                        return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Saldo insuficiente"));
                    }
                    account.setSaldo(account.getSaldo() - request.getMonto());
                    return repository.actualizarCuenta(account);
                });
    }

    @Override
    public Mono<Void> transfer(TransferRequest request) {
        return repository.getAccountById(request.getCuentaOrigenId())
                .flatMap(origen -> {
                    if (!origen.getMiembrosUid().contains(request.getUsuarioUid())) {
                        return Mono.error(new CustomException(HttpStatus.FORBIDDEN, "Usuario no pertenece a la cuenta de origen"));
                    }
                    if (origen.getSaldo() < request.getMonto()) {
                        return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Saldo insuficiente en cuenta de origen"));
                    }
                    return repository.getAccountById(request.getCuentaDestinoId())
                            .flatMap(destino -> {
                                origen.setSaldo(origen.getSaldo() - request.getMonto());
                                destino.setSaldo(destino.getSaldo() + request.getMonto());

                                return Mono.when(
                                        repository.actualizarCuenta(origen),
                                        repository.actualizarCuenta(destino),
                                        notificationService.notifyTransfer(
                                                request.getCuentaOrigenId(),
                                                request.getCuentaDestinoId(),
                                                request.getUsuarioUid(),
                                                request.getMonto()
                                        )
                                );
                            });
                });
    }

}
