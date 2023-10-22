package com.example.appearanceRater.event;

import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.user.UserEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ResentActivationEvent extends ApplicationEvent {
    private final UserEntity user;
    private final Token token;
    private final Token expiredToken;

    public ResentActivationEvent(Object source, UserEntity user, Token token, Token expiredToken) {
        super(source);
        this.user = user;
        this.token = token;
        this.expiredToken = expiredToken;
    }
}
