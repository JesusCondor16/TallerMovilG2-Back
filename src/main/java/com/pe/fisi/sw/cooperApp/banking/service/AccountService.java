package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.dto.ReportRequest;
import com.pe.fisi.sw.cooperApp.notifications.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AccountService {
    Mono<AccountResponse> createAccount(CreateAccountRequest request);
    Mono<List<AccountResponse>> getAllAcountsOwnerOfByUuid(String uuid);
    Mono<List<AccountResponse>> getAllAcountsMemberOfByUuid(String uuid);
    Mono<List<AccountUserDto>> getAllMembersOfByAccountId(String accountId);
    Mono<String> generateCode(String accountId);
    Mono<AccountResponse> getAccountDetails(String cuentauid);

    Mono<NotificationEvent> reportAccount(ReportRequest request, List<FilePart> files);
}
