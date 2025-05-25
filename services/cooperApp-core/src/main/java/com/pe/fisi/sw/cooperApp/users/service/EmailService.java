package com.pe.fisi.sw.cooperApp.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String sender;
    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();
    public String generateCode(String email) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        resetCodes.put(email, code);
        return code;
    }

    public void sendCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email);
        message.setSubject("Cooper App: Codigo reinicio de contraseña");
        message.setText("Tu codigo de verificacion es: "+ code);
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        return code.equals(resetCodes.get(email));
    }

    public void invalidateCode(String email) {
        resetCodes.remove(email);
    }
}
