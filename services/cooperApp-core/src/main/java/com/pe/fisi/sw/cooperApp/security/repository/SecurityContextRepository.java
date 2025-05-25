package com.pe.fisi.sw.cooperApp.security.repository;

import com.pe.fisi.sw.cooperApp.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String token = exchange.getAttribute("token");

        if(token == null) { return Mono.empty(); }
        return Mono.fromCallable(() -> JwtProvider.verifyToken(token))
                .map(firebaseToken -> {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            firebaseToken.getUid(),
                            null,
                            List.of()
                    );
                    return (SecurityContext) new SecurityContextImpl(auth);
                })
                .onErrorResume(e->Mono.empty());
    }
}
