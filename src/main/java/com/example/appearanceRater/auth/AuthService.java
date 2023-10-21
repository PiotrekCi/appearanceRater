package com.example.appearanceRater.auth;

import com.example.appearanceRater.event.RegistrationCompleteEvent;
import com.example.appearanceRater.exception.CredentialsTakenException;
import com.example.appearanceRater.exception.InvalidTokenException;
import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.token.TokenRepository;
import com.example.appearanceRater.jwt.JwtService;
import com.example.appearanceRater.user.Role;
import com.example.appearanceRater.user.UserEntity;
import com.example.appearanceRater.user.UserRegistrationForm;
import com.example.appearanceRater.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.example.appearanceRater.token.TokenType.ACTIVATING;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final ApplicationContext applicationContext;
    public AuthenticationResponse register(UserRegistrationForm userRegistrationForm) {
        boolean emailExists = userRepository.findByEmail(userRegistrationForm.getEmail()).isPresent();
        boolean usernameExists = userRepository.findByUsername(userRegistrationForm.getUsername()).isPresent();
        if (emailExists || usernameExists) {
            throw new CredentialsTakenException(emailExists && usernameExists
                    ? "Username and Email are already taken."
                    : emailExists ? "Email is already taken."
                    : "Username is already taken."
            );
        }
        UserEntity user = userRepository.save(
                UserEntity.builder()
                        .email(userRegistrationForm.getEmail())
                        .password(passwordEncoder.encode(userRegistrationForm.getPassword()))
                        .username(userRegistrationForm.getUsername())
                        .role(Role.USER)
                        .enabled(false)
                        .accountNonLocked(true)
                        .build()
        );

        String token = jwtService.generateToken(user);


        Token activatingToken = tokenRepository.save(Token.builder()
                .expired(false)
                .revoked(false)
                .token(token)
                .user(user)
                .type(ACTIVATING)
                .build()
        );

        applicationContext.publishEvent(new RegistrationCompleteEvent(this, user, activatingToken));

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken("")
                .build();
    }

    public void activate(String token) {
        if (token == null) {
            throw new InvalidTokenException("Invalid token.");
        }

        Token activatingToken = tokenRepository.findRegistrationToken(token).orElseThrow(() -> new InvalidTokenException("Token doesn't exist."));
        Date expiration = jwtService.extractExpiration(activatingToken.getToken());

        if (expiration.after(Date.from(Instant.now().plus(23, ChronoUnit.HOURS).plus(56, ChronoUnit.MINUTES)))) {
          System.out.println("Expired token");
          //ToDo resent email with new activating token
          return;
        }

        activatingToken.getUser().setEnabled(true);
        tokenRepository.delete(activatingToken);
    }
}
