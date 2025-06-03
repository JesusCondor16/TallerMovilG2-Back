package com.pe.fisi.sw.cooperApp.security.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.dto.TokenResponse;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthService {
    private final WebClient webClient;
    @Value("${firebase.api-key}")
    private String firebaseApiKey;
    @Value("${firebase.auth-url}")
    private String firebaseAuthUrl;
    @Value("${firebase.token-refresh-url}")
    private String firebaseRefreshUrl;
    private final UserService userService;

    public Mono<TokenResponse> register(RegisterRequest request) {
        return Mono.zip(
                userService.validateDni(request.getDni()),
                userService.validateEmail(request.getEmail())
        ).flatMap(validationResults -> {
            boolean dniExists = validationResults.getT1();
            boolean emailExists = validationResults.getT2();

            if (dniExists) {
                return Mono.error(new CustomException(HttpStatus.CONFLICT, "DNI ya registrado"));
            }
            if (emailExists) {
                return Mono.error(new CustomException(HttpStatus.CONFLICT, "Email ya registrado"));
            }

            // Si pasa validación, crear usuario en Firebase Auth
            return Mono.fromCallable(() -> {
                try {
                    return FirebaseAuth.getInstance().createUser(
                            new UserRecord.CreateRequest()
                                    .setEmail(request.getEmail())
                                    .setPassword(request.getPassword())
                                    .setEmailVerified(false)
                                    .setDisabled(false)
                    );
                } catch (Exception e) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "Error en Firebase: " + e.getMessage());
                }
            }).flatMap(userRecord ->
                    userService.createUserFirestore(userRecord.getUid(), request)
                            .then(this.login(request.getEmail(), request.getPassword()))
                            .onErrorResume(e ->
                                    Mono.fromCallable(() -> {
                                        FirebaseAuth.getInstance().deleteUser(userRecord.getUid());
                                        return null;
                                    }).then(Mono.error(new CustomException(HttpStatus.BAD_REQUEST,
                                            "Error al guardar en Firestore: " + e.getMessage())))
                            )
            );
        });
    }


    public Mono<TokenResponse> login(String email, String password) {
        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        return webClient.post()
                .uri(firebaseAuthUrl + firebaseApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String idToken = (String) response.get("idToken");
                    String refreshToken = (String) response.get("refreshToken");
                    return new TokenResponse(idToken, refreshToken);
                })
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.error(new CustomException(HttpStatus.UNAUTHORIZED, "Contraseña o usuario invalidos "))
                );
    }

    public Mono<TokenResponse> refreshToken(String refreshToken) {
        Map<String, Object> requestBody = Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
        );

        return webClient.post()
                .uri(firebaseRefreshUrl + firebaseApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String idToken = (String) response.get("id_token");
                    String newRefreshToken = (String) response.get("refresh_token");
                    return new TokenResponse(idToken, newRefreshToken);
                })
                .onErrorResume(WebClientResponseException.class, e ->
                        Mono.error(new CustomException(HttpStatus.UNAUTHORIZED, "Token refresh invalido"))
                );
    }
    public Mono<String> updatePassword(String email, String newPassword) {
        return Mono.fromCallable(() -> {
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUid())
                    .setPassword(newPassword);
            FirebaseAuth.getInstance().updateUser(request);
            return "Contraseña actualizada";
        });
    }
    public Mono<String> getUidByEmail(String email) {
        return Mono.fromCallable(() -> {
            try {
                UserRecord user = FirebaseAuth.getInstance()
                        .getUserByEmail(email);
                return user.getUid();
            } catch (Exception e) {
                throw new CustomException(HttpStatus.NOT_FOUND, "Usuario no encontrado con email: " + email);
            }
        });
    }
    public Mono<String> getEmailByUid(String uid) {
        return Mono.fromCallable(() -> {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            return userRecord.getEmail();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
