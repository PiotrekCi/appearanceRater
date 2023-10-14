package com.example.appearanceRater.auth;

import com.example.appearanceRater.user.UserRegistrationForm;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/register")
    public <T> ResponseEntity<T> register(
           @RequestBody @Valid UserRegistrationForm userRegistrationForm
    ) {
        return ResponseEntity.ok().build();
    }
}
