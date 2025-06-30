package com.pe.fisi.sw.cooperApp.files.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveFileService {
    public final Drive driveService;

    /**
     * Sube los archivos a la carpeta especificada
     */
    public List<String> uploadFilesToFolder(List<java.io.File> files, String folderId) throws IOException {
        List<String> urls = new ArrayList<>();

        for (java.io.File file : files) {
            if (!file.exists() || !file.canRead()) {
                throw new IOException("No se puede leer el archivo: " + file.getAbsolutePath());
            }

            String fileUrl = uploadSingleFile(file, folderId);
            urls.add(fileUrl);

            log.debug("Archivo subido: {} -> {}", file.getName(), fileUrl);
        }

        return urls;
    }

    /**
     * Sube un archivo individual a Google Drive
     */
    public String uploadSingleFile(java.io.File file, String folderId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        // Agregar timestamp al nombre del archivo para evitar conflictos
        String fileName = addTimestampToFileName(file.getName());
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(getMimeType(file), file);

        com.google.api.services.drive.model.File uploadedFile = driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        // Hacer el archivo público
        makeFilePublic(uploadedFile.getId());

        return generateFileUrl(uploadedFile.getId());
    }

    /**
     * Agrega timestamp al nombre del archivo para evitar conflictos
     */
    public String addTimestampToFileName(String originalName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        int lastDotIndex = originalName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return originalName + "_" + timestamp;
        }

        String nameWithoutExtension = originalName.substring(0, lastDotIndex);
        String extension = originalName.substring(lastDotIndex);
        return nameWithoutExtension + "_" + timestamp + extension;
    }

    /**
     * Hace un archivo público para que cualquiera con el enlace pueda verlo
     */
    public void makeFilePublic(String fileId) throws IOException {
        com.google.api.services.drive.model.Permission permission =
                new com.google.api.services.drive.model.Permission()
                        .setType("anyone")
                        .setRole("reader");

        driveService.permissions()
                .create(fileId, permission)
                .execute();

        log.debug("Archivo {} configurado como público", fileId);
    }


    /**
     * Genera la URL de visualización del archivo en Google Drive
     */
    public String generateFileUrl(String fileId) {
        return String.format("https://drive.google.com/file/d/%s/view", fileId);
    }

    /**
     * Detecta el tipo MIME del archivo
     */
    public String getMimeType(java.io.File file) {
        try {
            String mimeType = java.nio.file.Files.probeContentType(file.toPath());
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            log.warn("No se pudo determinar el tipo MIME para: {}", file.getName());
            return "application/octet-stream";
        }
    }
}
