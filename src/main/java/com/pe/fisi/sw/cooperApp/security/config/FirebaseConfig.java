package com.pe.fisi.sw.cooperApp.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            // Cambiado para leer desde /etc/secrets en Render
            InputStream serviceAccount = new java.io.FileInputStream("/etc/secrets/cooperauthapp-firebase-admin.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://cooperauthapp-default-rtdb.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("FirebaseApp inicializado correctamente.");
            } else {
                logger.info("FirebaseApp ya estaba inicializado.");
            }

        } catch (IOException e) {
            logger.error("Error inicializando FirebaseApp: {}", e.getMessage());
        }
    }

    @Bean
    public Firestore firestore(){
        return FirestoreClient.getFirestore();
    }
}
