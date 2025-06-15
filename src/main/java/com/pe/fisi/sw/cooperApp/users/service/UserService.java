package com.pe.fisi.sw.cooperApp.users.service;

import com.pe.fisi.sw.cooperApp.users.dto.EditRequest;
import com.pe.fisi.sw.cooperApp.users.mapper.UserMapper;
import com.pe.fisi.sw.cooperApp.users.model.User;
import com.pe.fisi.sw.cooperApp.security.dto.RegisterRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<Void> createUserFirestore(String uid, RegisterRequest request) {
        User user = userMapper.fromRegisterRequest(uid, request);
        log.info(request.getFechaNacimiento());
        return userRepository.save(user)
                .onErrorResume(e ->
                        Mono.error(new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Error al guardar el usuario en Firestore: " + e.getMessage()))
                );
    }

    public Mono<Boolean> validateDni(String dni) {
        return userRepository.existsByDni(dni);
    }

    public Mono<Boolean> validateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Mono<User> findByUid(String uid) {
        return userRepository.findById(uid);
    }

    public Mono<User> editProfile(EditRequest editRequest) {
        return userRepository.updateUser(editRequest);
    }
}
