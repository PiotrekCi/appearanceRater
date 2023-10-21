package com.example.appearanceRater.event;

import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.user.UserEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private final UserEntity user;
    private final Token token;

    public RegistrationCompleteEvent(Object source, UserEntity user, Token token) {
        super(source);
        this.user = user;
        this.token = token;
    }
}
