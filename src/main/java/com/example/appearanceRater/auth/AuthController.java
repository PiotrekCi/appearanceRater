package com.example.appearanceRater.auth;

import com.example.appearanceRater.user.UserRegistrationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
           @RequestBody @Valid UserRegistrationForm userRegistrationForm
    ) {
        return ResponseEntity.ok(
                authService.register(userRegistrationForm)
        );
    }
}
