package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.ReportRequest;
import com.pe.fisi.sw.cooperApp.banking.repository.AccountRepository;
import com.pe.fisi.sw.cooperApp.files.service.FileUploader;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import com.pe.fisi.sw.cooperApp.notifications.service.NotificationService;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountReportServiceImpl implements AccountReportService {
    private final AccountRepository repository;
    private final FileUploader uploader;
    private final NotificationService notificationService;
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
                                        return Mono.error(new CustomException(HttpStatus.BAD_REQUEST,"Error subiendo archivos a Google Drive" + e));
                                    }

                                    // Eliminar archivos temporales
                                    tempFiles.forEach(java.io.File::delete);

                                    return notificationService.notifyAccountReport(
                                            request.getCuentaUid(),
                                            request.getReporterId(),
                                            ownerUid,
                                            request.getMotivo(),
                                            urlsConcatenadas
                                    );
                                })
                );
    }
}
