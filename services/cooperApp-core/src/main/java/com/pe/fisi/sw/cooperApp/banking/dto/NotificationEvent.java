package com.pe.fisi.sw.cooperApp.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class NotificationEvent {
    private String userId;
    private String message;
    private String type;
    private String referenceId;
    private String status;
}
