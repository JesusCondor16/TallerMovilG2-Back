package com.pe.fisi.sw.cooperApp.files.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriveFolderService {
    private final Drive driveService;

    // ID de la carpeta compartida que creaste
    private static final String SHARED_FOLDER_ID = "1R7wwF26VwkptDDkLiCsGbudQHaI8nkn0";

    /**
     * Crea una subcarpeta dentro de la carpeta compartida
     */
    public String createSubFolder(String cuentaUid) throws IOException {
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
     * Valida los parámetros de entrada
     */
    public void validateInputs(List<java.io.File> files, String cuentaUid) {
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

    /**
     * Genera el nombre de la subcarpeta basado en cuenta y timestamp
     */
    public String generateFolderName(String cuentaUid) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("report_%s_%s", cuentaUid, timestamp);
    }

}
