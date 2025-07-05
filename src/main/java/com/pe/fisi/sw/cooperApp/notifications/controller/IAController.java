package com.pe.fisi.sw.cooperApp.notifications.controller;

import com.pe.fisi.sw.cooperApp.notifications.service.IAService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/ia")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;

    @PostMapping("/analizar-comportamiento")
    public Mono<IAService.IAResponse> analizarComportamiento(@RequestBody IAService.InputIA input) {
        return iaService.analizarComportamiento(input);
    }
}