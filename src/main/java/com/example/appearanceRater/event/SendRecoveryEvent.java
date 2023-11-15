package com.example.appearanceRater.event;

import com.example.appearanceRater.token.Token;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendRecoveryEvent extends ApplicationEvent {
    private final String userEmail;
    private final Token recoveryToken;

    public SendRecoveryEvent(Object source, String userEmail, Token recoveryToken) {
        super(source);
        this.userEmail = userEmail;
        this.recoveryToken = recoveryToken;
    }
}
