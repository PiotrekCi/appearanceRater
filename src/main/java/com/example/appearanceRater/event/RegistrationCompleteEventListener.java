package com.example.appearanceRater.event;

import com.example.appearanceRater.mailer.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    private final EmailService emailService;

    @Override
    @Async
    public void onApplicationEvent(RegistrationCompleteEvent registrationCompleteEvent) {
        emailService.sendRegistrationEmail(registrationCompleteEvent.getUser(), registrationCompleteEvent.getToken());
    }
}
