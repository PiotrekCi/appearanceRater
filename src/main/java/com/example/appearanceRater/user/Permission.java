package com.example.appearanceRater.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {
    USER_READ("user:read");

    @Getter
    private final String permission;
}
