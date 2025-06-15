package com.pe.fisi.sw.cooperApp.files.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleUploadDriveService  implements FileUploader {
    private final DriveFolderService folderService;
    private final GoogleDriveFileService fileService;
    /**
     * Sube una lista de archivos a Google Drive dentro de una subcarpeta específica
     * @param files Lista de archivos a subir
     * @param cuentaUid Identificador de la cuenta para nombrar la subcarpeta
     * @return String con las URLs de los archivos subidos separadas por salto de línea
     * @throws IOException Si hay error en la subida
     */
    public String uploadFiles(List<java.io.File> files, String cuentaUid) throws IOException {
        folderService.validateInputs(files, cuentaUid);
        String subFolderId = folderService.createSubFolder(cuentaUid);
        List<String> fileUrls = fileService.uploadFilesToFolder(files,subFolderId);
        return String.join("\n", fileUrls);
    }
}