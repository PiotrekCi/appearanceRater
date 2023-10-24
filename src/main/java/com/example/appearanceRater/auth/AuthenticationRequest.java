package com.example.appearanceRater.auth;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
public class AuthenticationRequest {
    private final String user;
    private final String password;
}
