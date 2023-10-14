package com.example.appearanceRater.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
abstract class User {
    @NotNull
    @Size(min = 5, max = 24)
    @Pattern(regexp = "^[a-zA-Z0-9äöüÄÖÜ_.-]*$")
    private String username;

    @NotNull
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).*$")
    private String password;

    @NotNull
    @Email
    private String email;
}
