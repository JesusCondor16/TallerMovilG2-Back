package com.pe.fisi.sw.cooperApp.security.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    private static final String APPLICATION_NAME = "cooperApp";
    private static final String CREDENTIALS_FILE = "google-drive.json";
    // Scope actualizado para incluir permisos
    private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        try {
            // Primero intentar desde el classpath
            InputStream credentialsStream = getClass().getClassLoader()
                    .getResourceAsStream(CREDENTIALS_FILE);

            if (credentialsStream == null) {
                // Si no está en classpath, intentar desde el directorio raíz
                credentialsStream = new ClassPathResource("../" + CREDENTIALS_FILE).getInputStream();
            }

            GoogleCredentials credentials;
            try (InputStream stream = credentialsStream) {
                credentials = GoogleCredentials.fromStream(stream)
                        .createScoped(Collections.singleton(DRIVE_SCOPE));
            }

            return credentials;

        } catch (IOException e) {
            throw new IOException("Error cargando credenciales de Google Drive: " + e.getMessage(), e);
        }
    }

    @Bean
    public Drive googleDriveService(GoogleCredentials credentials) {
        try {
            // Verificar que las credenciales estén configuradas correctamente
            if (credentials == null) {
                throw new IllegalStateException("Las credenciales de Google no pueden ser null");
            }

            return new Drive.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error creando servicio de Google Drive: " + e.getMessage(), e);
        }
    }
}