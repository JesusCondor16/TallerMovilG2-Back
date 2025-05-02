package com.pe.fisi.sw.cooperApp.security.validator;

import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomValidator {
    private final Validator validator;
    private final MessageSource messageSource;
    @SneakyThrows
    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> errors = validator.validate(object);
        if (errors.isEmpty()) {
            log.info("No se encontraron errores de validacion");
        } else {
            String message = errors
                    .stream()
                    .map(violation -> {
                        if (violation.getMessage().contains("{field}"))
                            return violation
                                    .getMessage()
                                    .replace(
                                            "{field}",
                                            violation.getPropertyPath().toString().toUpperCase()
                                    );
                        return violation.getMessage();
                    })
                    .collect(Collectors.joining(", "));

            log.error("Validation errors: {}", message);
            throw new CustomException(HttpStatus.BAD_REQUEST, message);
        }
    }

}