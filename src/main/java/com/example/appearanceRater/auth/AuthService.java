package com.example.appearanceRater.auth;

import com.example.appearanceRater.event.RegistrationCompleteEvent;
import com.example.appearanceRater.event.ResentActivationEvent;
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
import org.springframework.web.servlet.ModelAndView;

import static com.example.appearanceRater.token.TokenType.ACTIVATING;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final ApplicationContext applicationContext;
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
        if (token == null) {
            throw new InvalidTokenException("Invalid token.");
        }

        ModelAndView modelAndView = new ModelAndView("VerificationPage.html");
        Token activatingToken = tokenRepository.findRegistrationToken(token).orElseThrow(() -> new InvalidTokenException("Token doesn't exist."));

        /**
            In case of someone clicking twice before Event sends mail and deletes token from repository we mark it with flag
            {@link com.example.appearanceRater.event.ResentActivationEventListener.java:21}
         */
        if (activatingToken.isRevoked()) {
            throw new InvalidTokenException("Invalid token.");
        }

        if (jwtService.isExpired(activatingToken)) {
            modelAndView.addObject("expired", true);
            modelAndView.addObject("token", activatingToken.getToken());
            activatingToken.setExpired(true);
            return modelAndView;
        }

        activatingToken.getUser().setEnabled(true);
        tokenRepository.delete(activatingToken);

        return modelAndView;
    }

    public ModelAndView resentVerification(String token) {
        System.out.println(token);
        ModelAndView modelAndView = new ModelAndView("ResentVerification.html");
        Token expiredToken = tokenRepository.findRegistrationToken(token).orElseThrow(() -> new InvalidTokenException("Token doesn't exist."));
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

        expiredToken.setRevoked(true);
        applicationContext.publishEvent(new ResentActivationEvent(this, user, activeToken, expiredToken));

        modelAndView.addObject("sentTo", user.getEmail());
        return modelAndView;
    }
}
