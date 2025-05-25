package com.pe.fisi.sw.cooperApp.security.controller;


import com.pe.fisi.sw.cooperApp.security.dto.*;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.security.service.FirebaseAuthService;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import com.pe.fisi.sw.cooperApp.users.service.EmailService;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final FirebaseAuthService firebaseAuthService;
    private final CustomValidator customValidator;
    private final EmailService emailService;
    private final UserService userService;
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
    @PostMapping("/reset-password/request")
    public Mono<ResponseEntity<Map<String, String>>> sendCode(@RequestBody PasswordResetRequest request) {
        return userService.validateEmail(request.getEmail())
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new CustomException(HttpStatus.UNAUTHORIZED, "Ese email no existe!"));
                    }

                    String code = emailService.generateCode(request.getEmail());
                    emailService.sendCode(request.getEmail(), code);
                    return Mono.just(ResponseEntity.ok(Map.of("message", "Código de verificación enviado al correo")));
                });
    }

    @PostMapping("/reset-password/confirm")
    public Mono<ResponseEntity<Map<String,String>>> confirm(@RequestBody PasswordResetConfirm confirm) {
        if (!emailService.verifyCode(confirm.getEmail(), confirm.getCode())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED,"Código incorrecto");
        }
        return firebaseAuthService.updatePassword(confirm.getEmail(), confirm.getNewPassword())
                .doOnSuccess(v -> emailService.invalidateCode(confirm.getEmail()))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Contraseña cambiada correctamente")));
    }
    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("Hola Mundo!");
    }
}
