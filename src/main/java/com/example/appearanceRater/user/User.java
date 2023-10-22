package com.example.appearanceRater.user;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@MappedSuperclass
@SuperBuilder
public abstract class User {
    @NotNull
    @Size(min = 5, max = 24)
    @Pattern(regexp = "^[a-zA-Z0-9äöüÄÖÜ_.-]*$")
    @Column(unique = true)
    private String username;

    @NotNull
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).*$")
    private String password;

    @NotNull
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @Column(unique = true)
    private String email;
}
