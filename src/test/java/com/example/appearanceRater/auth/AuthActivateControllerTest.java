package com.example.appearanceRater.auth;

import com.example.appearanceRater.exception.InvalidTokenException;
import com.example.appearanceRater.jwt.JwtService;
import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.token.TokenRepository;
import com.example.appearanceRater.token.TokenType;
import com.example.appearanceRater.user.UserEntity;
import com.example.appearanceRater.user.repository.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthActivateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @SpyBean
    private UserRepository userRepository;
    @SpyBean
    private TokenRepository tokenRepository;
    @MockBean
    private JwtService jwtService;

    @Test
    @SneakyThrows
    @DirtiesContext
    void shouldActivateUserAccount() {
        //given
        UserEntity user = user();
        Token activatingToken = activatingToken();
        activatingToken.setUser(user);
        userRepository.save(user);
        tokenRepository.save(activatingToken);
        when(jwtService.isExpired(activatingToken.getToken()))
                .thenReturn(false);
        //when&then
        mockMvc.perform(get("/api/v1/auth/activate")
                        .param("token", activatingToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("VerificationPage.html"))
                .andExpect(model().attributeDoesNotExist("token", "expired"));

        assertTrue(userRepository.findByEmail(user.getEmail()).get().isEnabled());
        assertTrue(tokenRepository.findRegistrationToken(activatingToken().getToken()).isEmpty());
    }

    @Test
    @SneakyThrows
    @DirtiesContext
    void shouldNotActivateUserAccountWhenTokenExpired() {
        //given
        UserEntity user = user();
        Token activatingToken = activatingToken();
        activatingToken.setUser(user);
        userRepository.save(user);
        tokenRepository.save(activatingToken);
        when(jwtService.isExpired(activatingToken.getToken()))
                .thenReturn(true);

        //when&then
        mockMvc.perform(get("/api/v1/auth/activate")
                        .param("token", activatingToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("VerificationPage.html"))
                .andExpect(model().attribute("expired", true))
                .andExpect(model().attribute("token", activatingToken.getToken()));

        assertTrue(tokenRepository.findRegistrationToken(activatingToken.getToken()).isPresent());
        assertTrue(tokenRepository.findRegistrationToken(activatingToken().getToken()).get().isExpired());
        assertTrue(tokenRepository.findRegistrationToken(activatingToken().getToken()).get().isRevoked());
        assertFalse(userRepository.findByEmail(user.getEmail()).get().isEnabled());
    }

    @Test
    @DirtiesContext
    void shouldThrowInvalidTokenExceptionWhenActivatingTokenIsRevoked() throws Exception {
        //given
        UserEntity user = user();
        Token activatingToken = activatingToken();
        activatingToken.setRevoked(true);
        tokenRepository.save(activatingToken);

        //when&then
        mockMvc.perform(get("/api/v1/auth/activate")
                        .param("token", activatingToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidTokenException))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), "Invalid token."));
    }

    private UserEntity user() {
        return UserEntity.builder()
                .enabled(false)
                .email("test@xyz.zy")
                .username("testxyz")
                .password("123456As#")
                .token(List.of(activatingToken()))
                .build();
    }

    private Token activatingToken() {
        return Token.builder()
                .token("tokentoken")
                .type(TokenType.ACTIVATING)
                .revoked(false)
                .expired(false)
                .build();
    }
}
