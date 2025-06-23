package com.pe.fisi.sw.cooperApp.banking.controller;

import com.pe.fisi.sw.cooperApp.banking.dto.*;
import com.pe.fisi.sw.cooperApp.banking.service.AccountReportService;
import com.pe.fisi.sw.cooperApp.banking.service.AccountService;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import com.pe.fisi.sw.cooperApp.security.validator.FileValidator;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class BankingController {

    private final AccountService accountService;
    private final CustomValidator validator;
    private final FileValidator fileValidator;
    private final AccountReportService accountReportService;
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
    @GetMapping("/get-account/{cuentaUid}")
    public Mono<ResponseEntity<AccountResponse>> getAccount(@PathVariable String cuentaUid) {
        return accountService.getAccountDetails(cuentaUid).map(ResponseEntity::ok);
    }
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<NotificationEvent>> reportAccount(
            @RequestPart("data") ReportRequest request,
            @RequestPart("files") List<FilePart> files) {

        if (files == null || files.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Se requiere al menos un archivo.");
        }

        if (files.size() > 3) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Solo se permiten hasta 3 archivos.");
        }
        fileValidator.validateFiles(files);
        return accountReportService.reportAccount(request, files)
                .map(ResponseEntity::ok);
    }
    @PostMapping("/deposit")
    public Mono<ResponseEntity<Void>> deposit(@RequestBody DepositRequest request) {
        return accountService.deposit(request).thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/withdraw")
    public Mono<ResponseEntity<Void>> withdraw(@RequestBody WithdrawRequest request) {
        return accountService.withdraw(request).thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/invest")
    public Mono<ResponseEntity<Void>> invest(@RequestBody InvestmentRequest request) {
        return accountService.invest(request).thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/transfer")
    public Mono<ResponseEntity<Void>> transfer(@RequestBody TransferRequest request) {
        return accountService.transfer(request)
                .thenReturn(ResponseEntity.ok().build());
    }
}
