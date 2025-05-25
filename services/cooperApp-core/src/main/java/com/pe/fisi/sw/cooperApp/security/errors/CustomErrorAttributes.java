package com.pe.fisi.sw.cooperApp.security.errors;
import com.pe.fisi.sw.cooperApp.security.exceptions.CustomException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
@Primary
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(
            ServerRequest request,
            ErrorAttributeOptions options
    ) {
        Map<String, Object> attrs = super.getErrorAttributes(request, options);
        Throwable t = getError(request);
        if (t instanceof CustomException ce) {
            attrs.put("status", ce.getStatus().value());
            attrs.put("message", ce.getMessage());
        }
        return attrs;
    }
}
