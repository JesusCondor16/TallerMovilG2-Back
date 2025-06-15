package com.pe.fisi.sw.cooperApp.users.controller;

import com.pe.fisi.sw.cooperApp.users.dto.EditRequest;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import com.pe.fisi.sw.cooperApp.security.validator.CustomValidator;
import com.pe.fisi.sw.cooperApp.users.model.User;
import com.pe.fisi.sw.cooperApp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CustomValidator validator;
    @GetMapping("/{uid}")
    public Mono<ResponseEntity<User>> getUserByUid(@PathVariable String uid){
        return userService.findByUid(uid).map(ResponseEntity::ok)
                .onErrorResume(error->
                        Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Usuario no encontrado")));
        }
        @PutMapping("/edit/{uid}")
        public Mono<ResponseEntity<User>> editUser(@RequestBody EditRequest editRequest){
            validator.validate(editRequest);
            return userService.editProfile(editRequest).map(ResponseEntity::ok)
                    .onErrorResume(error->
                            Mono.error(new CustomException(HttpStatus.BAD_REQUEST, "Error al editar el usuario")));
        }
}
