package com.example.appearanceRater.mailer;

import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final SimpleMailMessage simpleMailMessage;
    private final JavaMailSender javaMailSender;
    private final String ACTIVATION_SUBJECT = "Activating Meech account.";
    public void sendRegistrationEmail(final UserEntity user, final Token token) {
        final String emailContent = String.format(
                "You've got 24 hours to activate your account via provided link. After 24 hours link will expire and you'll have to resent activation email.\n%s",
                "http://localhost:8080/api/v1/auth/activate?token=" + token.getToken()
        );

        simpleMailMessage.setSubject(ACTIVATION_SUBJECT);
        simpleMailMessage.setText(emailContent);
        simpleMailMessage.setTo(user.getEmail());

        javaMailSender.send(simpleMailMessage);
        log.info("ACTIVATING EMAIL SENT TO: " + user.getEmail() + " AT: " + LocalDateTime.now());
    }
}
