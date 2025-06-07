package com.pe.fisi.sw.cooperApp.security.validator;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileValidator {

    public void validateFiles(List<FilePart> files) {
        final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
        final long MAX_TOTAL_SIZE = 15 * 1024 * 1024; // 15MB
        final int MAX_FILES = 3;
        final List<String> tiposPermitidos = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp","application/pdf");
        final List<String> formatosPermitidos = Arrays.asList("jpg", "jpeg", "png", "gif", "webp","pdf");

        if (files == null || files.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Se requiere al menos un archivo.");
        }

        if (files.size() > MAX_FILES) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    String.format("Solo se permiten hasta %d archivos. Recibidos: %d", MAX_FILES, files.size()));
        }

        long totalSize = 0;
        for (FilePart file : files) {
            String filename = file.filename();
            if (filename.trim().isEmpty()) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Archivo con nombre vacío detectado.");
            }

            String contentType = file.headers().getContentType() != null ?
                    file.headers().getContentType().toString() : "unknown";

            if (!tiposPermitidos.contains(contentType)) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        String.format("Tipo no permitido: %s (%s)", contentType, filename));
            }

            String extension = getFileExtension(filename).toLowerCase();
            if (!formatosPermitidos.contains(extension)) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        String.format("Extensión no válida: %s (%s)", extension, filename));
            }

            long contentLength = file.headers().getContentLength();
            if (contentLength > 0) {
                if (contentLength > MAX_FILE_SIZE) {
                    throw new CustomException(HttpStatus.BAD_REQUEST,
                            String.format("Archivo '%s' muy grande: %.1f MB (máx. %.1f MB)",
                                    filename, contentLength / (1024.0 * 1024.0), MAX_FILE_SIZE / (1024.0 * 1024.0)));
                }
                totalSize += contentLength;
            }
        }

        if (totalSize > MAX_TOTAL_SIZE) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    String.format("Tamaño total muy grande: %.1f MB (máx. %.1f MB)",
                            totalSize / (1024.0 * 1024.0), MAX_TOTAL_SIZE / (1024.0 * 1024.0)));
        }

        log.info("Validados {} archivos correctamente", files.size());
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }
}
