package com.example.appearanceRater.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredentialsTakenException.class)
    public ResponseEntity<ExceptionResponse> handleCredentialsTakenException(CredentialsTakenException credentialsTakenException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                credentialsTakenException.getMessage(),
                credentialsTakenException.getClass(),
                Arrays.toString(credentialsTakenException.getStackTrace())
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidTokenException(InvalidTokenException invalidTokenException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                invalidTokenException.getMessage(),
                invalidTokenException.getClass(),
                Arrays.toString(invalidTokenException.getStackTrace())
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
