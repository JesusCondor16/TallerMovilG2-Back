package com.pe.fisi.sw.cooperApp.banking.service;

import com.google.cloud.firestore.Firestore;
import com.pe.fisi.sw.cooperApp.banking.dto.Account;
import com.pe.fisi.sw.cooperApp.banking.dto.AccountResponse;
import com.pe.fisi.sw.cooperApp.banking.dto.CreateAccountRequest;
import com.pe.fisi.sw.cooperApp.banking.dto.ReportRequest;
import com.pe.fisi.sw.cooperApp.banking.mapper.AccountMapper;
import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.notifications.dto.NotificationEvent;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.dto.AccountUserDto;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final UserService userService;
    private final Firestore firestore;
    private final AccountMapper accountMapper;
    private final GoogleUploadDriveService uploader;
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

    @Override
    public Mono<String> generateCode(String cuentaId) {
        long expiration = System.currentTimeMillis() + 3600_000;

        return repository.getAccountById(cuentaId)
                .map(account -> {
                    String email = account.getCreador().getEmail();
                    String raw = cuentaId + ":" + expiration + ":" + email;
                    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
                });
    }

    @Override
    public Mono<AccountResponse> getAccountDetails(String cuentauid) {
        return repository.getAccountById(cuentauid).map(accountMapper::toResponse);
    }

    @Override
    public Mono<NotificationEvent> reportAccount(ReportRequest request, List<FilePart> files) {
        return repository.getOwnerUidOfAccount(request.getCuentaUid())
                .flatMap(ownerUid ->
                        Flux.fromIterable(files)
                                .flatMap(filePart -> {
                                    // Guardar temporalmente cada archivo
                                    return Mono.fromCallable(() -> {
                                        java.io.File tempFile = java.io.File.createTempFile("upload-", filePart.filename());
                                        return tempFile;
                                    }).flatMap(tempFile ->
                                            filePart.transferTo(tempFile.toPath())
                                                    .thenReturn(tempFile)
                                    );
                                })
                                .collectList()
                                .flatMap(tempFiles -> {
                                    // Subir a Google Drive

                                    String urlsConcatenadas;
                                    try {
                                        urlsConcatenadas = uploader.uploadFiles(tempFiles, request.getCuentaUid());
                                    } catch (IOException e) {
                                        return Mono.error(new RuntimeException("Error subiendo archivos a Google Drive", e));
                                    }

                                    // Eliminar archivos temporales
                                    tempFiles.forEach(java.io.File::delete);

                                    // Crear mensaje
                                    String mensaje = "Motivo del reporte: " + request.getMotivo()
                                            + "\nArchivos adjuntos:\n" + urlsConcatenadas;

                                    // Crear notificación
                                    NotificationEvent notification = NotificationEvent.builder()
                                            .idNotification(UUID.randomUUID().toString())
                                            .idCuenta(request.getCuentaUid())
                                            .idSolcitante(request.getReporterId())
                                            .idUsuario(ownerUid)
                                            .mensaje(mensaje)
                                            .estado("pending")
                                            .tipo("reporte")
                                            .fechaCreacion(Instant.now())
                                            .fechaModificacion(Instant.now())
                                            .build();

                                    // Guardar en Firestore (bloqueante, por eso en boundedElastic)
                                    return Mono.fromCallable(() -> {
                                        firestore.collection("Notifications")
                                                .document(notification.getIdNotification())
                                                .set(notification)
                                                .get();
                                        return notification;
                                    }).subscribeOn(Schedulers.boundedElastic());
                                })
                );
    }
}
