package com.example.appearanceRater.auth;

import com.example.appearanceRater.user.UserRegistrationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    public AuthenticationResponse register(UserRegistrationForm userRegistrationForm) {
        return AuthenticationResponse.builder().build();
    }
}
