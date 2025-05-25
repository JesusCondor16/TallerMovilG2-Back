package com.pe.fisi.sw.cooperApp.cooperApp_notificacion_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FirebaseConfigNotifications {

    private static final String NOTIFICATIONS_APP_NAME = "notifications-app";

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/cooperauthapp-firebase-admin.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://cooperauthapp-default-rtdb.firebaseio.com")
                    .build();

            // Verificar si ya existe una app con este nombre
            boolean appExists = FirebaseApp.getApps().stream()
                    .anyMatch(app -> NOTIFICATIONS_APP_NAME.equals(app.getName()));

            if (!appExists) {
                FirebaseApp.initializeApp(options, NOTIFICATIONS_APP_NAME);
                log.info("FirebaseApp '{}' inicializado correctamente.", NOTIFICATIONS_APP_NAME);
            } else {
                log.info("FirebaseApp '{}' ya estaba inicializado.", NOTIFICATIONS_APP_NAME);
            }

        } catch (IOException e) {
            log.error("Error inicializando FirebaseApp: {}", e.getMessage());
        }
    }

    @Bean
    public Firestore firestore(){
        return FirestoreClient.getFirestore(FirebaseApp.getInstance(NOTIFICATIONS_APP_NAME));
    }
}