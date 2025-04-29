package com.pe.fisi.sw.cooperApp.security.controller;


import com.pe.fisi.sw.cooperApp.security.dto.LoginRequest;
import com.pe.fisi.sw.cooperApp.security.dto.RefreshTokenRequest;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.dto.TokenResponse;

import com.pe.fisi.sw.cooperApp.security.service.FirebaseAuthService;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final FirebaseAuthService firebaseAuthService;
    private final CustomValidator customValidator;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TokenResponse> register(@RequestBody RegisterRequest request) {
        customValidator.validate(request);
        return firebaseAuthService.register(request);
    }
    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody LoginRequest request) {
        customValidator.validate(request);
        return firebaseAuthService.login(request.getEmail(), request.getPassword())
                .map(ResponseEntity::ok);
    }
    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        customValidator.validate(request);
        return firebaseAuthService.refreshToken(request.getRefreshToken())
                .map(ResponseEntity::ok);
    }
    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("Hola Mundo!");
    }
}
