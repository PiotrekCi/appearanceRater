package com.example.appearanceRater.auth;

import com.example.appearanceRater.user.UserRegistrationForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
           @RequestBody @Valid UserRegistrationForm userRegistrationForm
    ) {
        authService.register(userRegistrationForm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activate")
    public ModelAndView activate(@RequestParam String token) {
        return authService.activate(token);
    }

    @PostMapping("/resend-verification")
    public ModelAndView resendVerification(@RequestParam String token) {
        return authService.resentVerification(token);
    }
}
