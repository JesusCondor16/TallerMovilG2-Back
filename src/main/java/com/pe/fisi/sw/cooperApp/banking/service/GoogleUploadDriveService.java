package com.pe.fisi.sw.cooperApp.banking.service;

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
public class GoogleUploadDriveService {

    private final Drive driveService;

    // ID de la carpeta compartida que creaste
    private static final String SHARED_FOLDER_ID = "1R7wwF26VwkptDDkLiCsGbudQHaI8nkn0";

    /**
     * Sube una lista de archivos a Google Drive dentro de una subcarpeta específica
     * @param files Lista de archivos a subir
     * @param cuentaUid Identificador de la cuenta para nombrar la subcarpeta
     * @return String con las URLs de los archivos subidos separadas por salto de línea
     * @throws IOException Si hay error en la subida
     */
    public String uploadFiles(List<java.io.File> files, String cuentaUid) throws IOException {
        validateInputs(files, cuentaUid);

        try {
            // Crear subcarpeta dentro de la carpeta compartida
            String subFolderId = createSubFolder(cuentaUid);
            List<String> fileUrls = uploadFilesToFolder(files, subFolderId);

            log.info("Se subieron {} archivos correctamente para la cuenta: {}", files.size(), cuentaUid);
            log.info("URLs de los archivos subidos: {}", String.join(", ", fileUrls));
            return String.join("\n", fileUrls);

        } catch (Exception e) {
            log.error("Error subiendo archivos a Google Drive para cuenta {}: {}", cuentaUid, e.getMessage());
            throw new IOException("Error subiendo archivos a Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una subcarpeta dentro de la carpeta compartida
     */
    private String createSubFolder(String cuentaUid) throws IOException {
        String subFolderName = generateFolderName(cuentaUid);

        File folderMetadata = new File();
        folderMetadata.setName(subFolderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        // IMPORTANTE: Establecer la carpeta padre como la carpeta compartida
        folderMetadata.setParents(Collections.singletonList(SHARED_FOLDER_ID));

        File folder = driveService.files()
                .create(folderMetadata)
                .setFields("id")
                .execute();

        log.debug("Subcarpeta creada en Google Drive: {} con ID: {}", subFolderName, folder.getId());
        log.info("Subcarpeta creada: {} - Ver en: https://drive.google.com/drive/folders/{}", subFolderName, folder.getId());
        return folder.getId();
    }

    /**
     * Sube los archivos a la carpeta especificada
     */
    private List<String> uploadFilesToFolder(List<java.io.File> files, String folderId) throws IOException {
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
    private String uploadSingleFile(java.io.File file, String folderId) throws IOException {
        File fileMetadata = new File();
        // Agregar timestamp al nombre del archivo para evitar conflictos
        String fileName = addTimestampToFileName(file.getName());
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(getMimeType(file), file);

        File uploadedFile = driveService.files()
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
    private String addTimestampToFileName(String originalName) {
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
    private void makeFilePublic(String fileId) throws IOException {
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
     * Genera el nombre de la subcarpeta basado en cuenta y timestamp
     */
    private String generateFolderName(String cuentaUid) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("report_%s_%s", cuentaUid, timestamp);
    }

    /**
     * Genera la URL de visualización del archivo en Google Drive
     */
    private String generateFileUrl(String fileId) {
        return String.format("https://drive.google.com/file/d/%s/view", fileId);
    }

    /**
     * Detecta el tipo MIME del archivo
     */
    private String getMimeType(java.io.File file) {
        try {
            String mimeType = java.nio.file.Files.probeContentType(file.toPath());
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            log.warn("No se pudo determinar el tipo MIME para: {}", file.getName());
            return "application/octet-stream";
        }
    }

    /**
     * Valida los parámetros de entrada
     */
    private void validateInputs(List<java.io.File> files, String cuentaUid) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("La lista de archivos no puede estar vacía");
        }

        if (cuentaUid == null || cuentaUid.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de cuenta no puede estar vacío");
        }

        // Validar que todos los archivos existan
        for (java.io.File file : files) {
            if (file == null) {
                throw new IllegalArgumentException("Uno de los archivos es null");
            }
            if (!file.exists()) {
                throw new IllegalArgumentException("El archivo no existe: " + file.getAbsolutePath());
            }
        }
    }
}