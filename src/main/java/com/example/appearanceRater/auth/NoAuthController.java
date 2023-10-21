package com.example.appearanceRater.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class NoAuthController {
    private final JavaMailSender javaMailSender;
    @GetMapping("/api/v1/auth/sender")
    public String getAfterRegister() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("mailer.meech@gmail.com");
        message.setTo("mailer.meech@gmail.com");
        message.setText("tekst");
        message.setSubject("sabdzekt");
        javaMailSender.send(message);
        return "afterRegister";
    }
}
