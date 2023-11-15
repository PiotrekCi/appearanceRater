package com.example.appearanceRater.event;

import com.example.appearanceRater.mailer.EmailService;
import com.example.appearanceRater.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendRecoveryEventListener implements ApplicationListener<SendRecoveryEvent> {
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    @Override
    @Async
    public void onApplicationEvent(SendRecoveryEvent sendRecoveryEvent) {
        emailService.sendRecoveryEmail(sendRecoveryEvent.getUserEmail(), sendRecoveryEvent.getRecoveryToken().getToken());
        tokenRepository.delete(sendRecoveryEvent.getRecoveryToken());
    }
}
