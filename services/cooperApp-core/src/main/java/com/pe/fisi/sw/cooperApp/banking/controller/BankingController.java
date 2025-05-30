package com.pe.fisi.sw.cooperApp.banking.controller;

import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.service.AccountService;
import com.pe.fisi.sw.cooperApp.banking.service.AccountServiceImpl;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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
    @GetMapping("/get-owner/{userUid}")
    public Mono<ResponseEntity<List<AccountResponse>>> getAllAccountsOwnerOf(@PathVariable String userUid) {
        return accountService.getAllAcountsOwnerOfByUuid(userUid).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/get-member/{userUid}")
    public Mono<ResponseEntity<List<AccountResponse>>> getAllCountsMemberOf(@PathVariable String userUid) {
        return accountService.getAllAcountsMemberOfByUuid(userUid).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/members/{cuentaId}")
    public Mono<ResponseEntity<List<AccountUserDto>>> getAllMembersOfAccount(@PathVariable String cuentaId) {
        return accountService.getAllMembersOfByAccountId(cuentaId).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("/generate-code")
    public Mono<ResponseEntity<String>> generateInviteCode(@RequestParam String cuentaId) {
        return accountService.generateCode(cuentaId).map(ResponseEntity::ok);
    }
}
