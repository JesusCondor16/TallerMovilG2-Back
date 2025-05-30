package com.pe.fisi.sw.cooperApp.cooperApp_gateway.config;

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
public class FirebaseConfigGateway {

    private static final String GATEWAY_APP = "gateway-app";

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
                    .anyMatch(app -> GATEWAY_APP.equals(app.getName()));

            if (!appExists) {
                FirebaseApp.initializeApp(options, GATEWAY_APP);
                log.info("FirebaseApp '{}' inicializado correctamente.", GATEWAY_APP);
            } else {
                log.info("FirebaseApp '{}' ya estaba inicializado.", GATEWAY_APP);
            }

        } catch (IOException e) {
            log.error("Error inicializando FirebaseApp: {}", e.getMessage());
        }
    }

    @Bean
    public Firestore firestore(){
        return FirestoreClient.getFirestore(FirebaseApp.getInstance(GATEWAY_APP));
    }
}