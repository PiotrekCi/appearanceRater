package com.example.appearanceRater.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
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
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    private String email;
}
