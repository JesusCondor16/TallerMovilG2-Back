package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<AccountResponse> createAccount(CreateAccountRequest request);
}
