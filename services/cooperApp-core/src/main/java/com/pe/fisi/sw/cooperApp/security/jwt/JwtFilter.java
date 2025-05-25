package com.pe.fisi.sw.cooperApp.security.jwt;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if(request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        if(path.contains("auth")) {
            return chain.filter(exchange);
        }
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null) {
            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST,"No Token was found"));
        }
        if (!auth.startsWith("Bearer ")) {
            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST,"Invalid Token"));
        }
        String token = auth.replace("Bearer ", "");
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }
}
