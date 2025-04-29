package com.pe.fisi.sw.cooperApp.security.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.dto.TokenResponse;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebaseAuthService {
    private final WebClient webClient;
    @Value("${firebase.api-key}")
    private String firebaseApiKey;
    @Value("${firebase.auth-url}")
    private String firebaseAuthUrl;
    @Value("${firebase.token-refresh-url}")
    private String firebaseRefreshUrl;

    public Mono<TokenResponse> register(RegisterRequest request) {
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(request.getEmail())
                .setPassword(request.getPassword())
                .setEmailVerified(false)
                .setDisabled(false);

        return Mono.fromCallable(() -> FirebaseAuth.getInstance().createUser(createRequest))
                .flatMap(userRecord -> this.login(request.getEmail(), request.getPassword()))
                .onErrorResume(e -> Mono.error(new CustomException(HttpStatus.BAD_REQUEST,"Error al crear usuario: " + e.getMessage())));
    }
    public Mono<TokenResponse> login(String email, String password) {
        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password,
                "returnSecureToken", true
        );

        return webClient.post()
                .uri(firebaseAuthUrl+firebaseApiKey)
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


}
