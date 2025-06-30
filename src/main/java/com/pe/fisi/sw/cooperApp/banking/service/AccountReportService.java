package com.pe.fisi.sw.cooperApp.banking.service;

import com.pe.fisi.sw.cooperApp.banking.dto.ReportRequest;
import com.pe.fisi.sw.cooperApp.notifications.model.NotificationEvent;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AccountReportService {
    Mono<NotificationEvent> reportAccount(ReportRequest request, List<FilePart> files);
}
