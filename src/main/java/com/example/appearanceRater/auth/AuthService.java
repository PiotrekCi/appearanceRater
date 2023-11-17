package com.example.appearanceRater.auth;

import com.example.appearanceRater.event.RegistrationCompleteEvent;
import com.example.appearanceRater.event.ResentActivationEvent;
import com.example.appearanceRater.event.SendRecoveryEvent;
import com.example.appearanceRater.exception.CredentialsTakenException;
import com.example.appearanceRater.exception.InvalidTokenException;
import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.token.TokenRepository;
import com.example.appearanceRater.jwt.JwtService;
import com.example.appearanceRater.user.RemindPasswordRequest;
import com.example.appearanceRater.user.Role;
import com.example.appearanceRater.user.UserEntity;
import com.example.appearanceRater.user.UserRegistrationForm;
import com.example.appearanceRater.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.request.InvalidRequestStateException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

import static com.example.appearanceRater.token.TokenType.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final ApplicationContext applicationContext;
    private final AuthenticationManager authenticationManager;

    public void register(UserRegistrationForm userRegistrationForm) {
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
    }

    public ModelAndView activate(String token) {
        ModelAndView modelAndView = new ModelAndView("VerificationPage.html");
        Token activatingToken = tokenRepository.findRegistrationToken(token).orElseThrow(() -> new InvalidTokenException("Token doesn't exist."));

        /**
            In case of someone clicking twice before Event sends mail and deletes token from repository we mark it with flag
            {@link com.example.appearanceRater.event.ResentActivationEventListener.java:21}
         */
        if (activatingToken.isRevoked()) {
            throw new InvalidTokenException("Invalid token.");
        }

        if (jwtService.isExpired(activatingToken.getToken())) {
            modelAndView.addObject("expired", true);
            modelAndView.addObject("token", activatingToken.getToken());
            activatingToken.setExpired(true);
            activatingToken.setRevoked(true);
            tokenRepository.save(activatingToken);
            return modelAndView;
        }

        activatingToken.getUser().setEnabled(true);
        userRepository.save(activatingToken.getUser());
        tokenRepository.delete(activatingToken);

        return modelAndView;
    }

    public ModelAndView resentVerification(String token) {
        ModelAndView modelAndView = new ModelAndView("ResentVerificationPage.html");
        Token expiredToken = tokenRepository.findRegistrationToken(token).orElseThrow(() -> new InvalidTokenException("Token does not exist."));
        UserEntity user = expiredToken.getUser();
        String newToken = jwtService.generateToken(user);
        Token activeToken = tokenRepository.save(Token.builder()
                .expired(false)
                .revoked(false)
                .token(newToken)
                .user(user)
                .type(ACTIVATING)
                .build()
        );

        applicationContext.publishEvent(new ResentActivationEvent(this, user, activeToken, expiredToken));
        expiredToken.setRevoked(true);
        modelAndView.addObject("sentTo", user.getEmail());
        return modelAndView;
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUser(),
                authenticationRequest.getPassword()
        ));
        UserEntity user = userRepository.findByCredentials(authenticationRequest.getUser()).orElseThrow();

        String token = jwtService.generateToken(user);
        clearUserAuthenticatingTokensState(user);

        tokenRepository.save(Token.builder()
                .token(token)
                .type(AUTHENTICATION)
                .revoked(false)
                .expired(false)
                .user(user)
                .build()
        );

        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok().body(
                AuthenticationResponse.builder()
                        .refreshToken(refreshToken)
                        .accessToken(token)
                        .roles(List.of(user.getRole()))
                        .build()
        );
    }

    @Transactional
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractSubject(refreshToken);
        if (userEmail != null) {
            final UserEntity user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                final String accessToken = jwtService.generateToken(user);
                clearUserAuthenticatingTokensState(user);
                tokenRepository.save(Token.builder()
                        .type(AUTHENTICATION)
                        .revoked(false)
                        .expired(false)
                        .token(accessToken)
                        .user(user)
                        .build()
                );
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .roles(List.of(user.getRole()))
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public void remindPassword(RemindPasswordRequest remindPasswordRequest) {
        UserEntity userEntity = userRepository.findByEmail(remindPasswordRequest.getEmail()).orElseThrow(
                () -> new InvalidRequestStateException("Email does not exists.")
        );
        final String newToken = jwtService.generateToken(userEntity);

        Token recoveryToken = tokenRepository.save(Token.builder()
                .token(newToken)
                .type(RECOVERY)
                .revoked(false)
                .expired(false)
                .user(userEntity)
                .build()
        );

        applicationContext.publishEvent(new SendRecoveryEvent(this, remindPasswordRequest.getEmail(), recoveryToken));
    }

    public ModelAndView recoverPassword(String token) {
        ModelAndView modelAndView = new ModelAndView("ChangePasswordPage.html");
        Token recoveryToken = tokenRepository.findRecoveryToken(token).orElseThrow(() -> new InvalidTokenException("Token does not exist."));

        if (jwtService.isExpired(token)) {
            modelAndView.addObject("expired", true);
            recoveryToken.setRevoked(true);
            recoveryToken.setExpired(true);
            tokenRepository.save(recoveryToken);
            return modelAndView;
        }

        modelAndView.addObject("token", recoveryToken.getToken());

        return modelAndView;
    }

    public ModelAndView recoveryPasswordChange(RecoveryPasswordChangeRequest recoveryPasswordChangeRequest) {
        ModelAndView modelAndView = new ModelAndView("ChangePasswordSuccessfulPage.html");
        Token recoveryToken = tokenRepository.findRecoveryToken(recoveryPasswordChangeRequest.getToken()).orElseThrow(() -> new InvalidTokenException("Token does not exist."));
        UserEntity userEntity = recoveryToken.getUser();

        if (!recoveryPasswordChangeRequest.getNewPassword().equals(recoveryPasswordChangeRequest.getNewPasswordConfirmation())) {
            throw new IllegalStateException("Different passwords provided");
        }

        if (passwordEncoder.matches(recoveryPasswordChangeRequest.getNewPassword(), userEntity.getPassword())) {
            throw new IllegalStateException("New password cannot be the same as old password");
        }

        recoveryToken.setRevoked(true);
        tokenRepository.save(recoveryToken);
        userEntity.setPassword(passwordEncoder.encode(recoveryPasswordChangeRequest.getNewPassword()));
        userRepository.save(userEntity);

        return modelAndView;
    }

    private void clearUserAuthenticatingTokensState(final UserEntity user) {
        tokenRepository.deleteAllInvalidTokens(user.getId());
    }
}
