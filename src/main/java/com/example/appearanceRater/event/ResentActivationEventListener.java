package com.example.appearanceRater.event;

import com.example.appearanceRater.mailer.EmailService;
import com.example.appearanceRater.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResentActivationEventListener implements ApplicationListener<ResentActivationEvent> {
    private final TokenRepository tokenRepository;
    private final EmailService emailService;


    @Override
    @Async
    public void onApplicationEvent(ResentActivationEvent event) {
        emailService.sendRegistrationEmail(event.getUser(), event.getToken());
        tokenRepository.delete(event.getExpiredToken());
    }
}
