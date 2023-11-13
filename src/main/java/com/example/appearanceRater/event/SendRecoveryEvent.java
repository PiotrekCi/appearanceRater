package com.example.appearanceRater.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendRecoveryEvent extends ApplicationEvent {
    private final String userEmail;
    private final String recoveryToken;

    public SendRecoveryEvent(Object source, String userEmail, String recoveryToken) {
        super(source);
        this.userEmail = userEmail;
        this.recoveryToken = recoveryToken;
    }
}
