package com.pe.fisi.sw.cooperApp.security.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{
    private final HttpStatus status;
    public CustomException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
