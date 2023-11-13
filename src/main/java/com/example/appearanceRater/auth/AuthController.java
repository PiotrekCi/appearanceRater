package com.example.appearanceRater.auth;

import com.example.appearanceRater.user.RemindPasswordRequest;
import com.example.appearanceRater.user.UserRegistrationForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    public <T> ResponseEntity<T> register(
           @RequestBody @Valid UserRegistrationForm userRegistrationForm
    ) {
        authService.register(userRegistrationForm);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/authenticate")
    private ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        return authService.authenticate(authenticationRequest);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authService.refreshToken(request, response);
    }

    @GetMapping("/activate")
    public ModelAndView activate(@RequestParam String token) {
        return authService.activate(token);
    }

    @PostMapping("/resend-verification")
    public ModelAndView resendVerification(@RequestParam String token) {
        return authService.resentVerification(token);
    }

    @PostMapping("/remind-password")
    public void remindPassword(
            @RequestBody @Valid RemindPasswordRequest remindPasswordRequest
    ) {
        authService.remindPassword(remindPasswordRequest);
    }
}
