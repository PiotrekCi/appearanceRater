package com.example.appearanceRater.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest,
            Principal principal
    ) {
        userService.changePassword(changePasswordRequest, principal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(
         @RequestBody @Valid ChangeEmailRequest changeEmailRequest,
         Principal principal
    ) {
        userService.changeEmail(changeEmailRequest, principal);
        return ResponseEntity.ok().build();
    }
}
