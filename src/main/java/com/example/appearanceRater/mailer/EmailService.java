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
    private final String RECOVERY_SUBJECT = "Recovering account password.";
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

    public void sendRecoveryEmail(String email, String token) {
        final String emailContent = String.format(
                "Requested for password change. If it's not your request ignore this email, link will expire in 24 hours.\n%s",
                "http://localhost:8080/api/v1/auth/recovery?token=" + token
        );

        simpleMailMessage.setSubject(RECOVERY_SUBJECT);
        simpleMailMessage.setText(emailContent);
        simpleMailMessage.setTo(email);

        javaMailSender.send(simpleMailMessage);
        log.info("RECOVERY EMAIL SENT TO: " + email + " AT: " + LocalDateTime.now());
    }
}
