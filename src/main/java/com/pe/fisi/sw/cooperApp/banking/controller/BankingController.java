package com.pe.fisi.sw.cooperApp.banking.controller;

import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.service.AccountService;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class BankingController {

    private final AccountService accountService;
    private final CustomValidator validator;
    @PostMapping("/create")
    public Mono<ResponseEntity<AccountResponse>> createAccount(@RequestBody CreateAccountRequest request) {
        validator.validate(request);
        return accountService.createAccount(request)
                .map(ResponseEntity::ok);
    }
}
