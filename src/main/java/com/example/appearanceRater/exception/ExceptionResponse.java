package com.example.appearanceRater.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ExceptionResponse {
    private final int status;
    private final LocalDateTime timestamp;
    private final String message;
    private final Class<? extends CredentialsTakenException> error;
    private final String stacktrace;
}
