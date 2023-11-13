package com.example.appearanceRater.auth;

import com.example.appearanceRater.exception.CredentialsTakenException;
import com.example.appearanceRater.token.TokenType;
import com.example.appearanceRater.user.Role;
import com.example.appearanceRater.user.UserEntity;
import com.example.appearanceRater.user.UserRegistrationForm;
import com.example.appearanceRater.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthRegisterControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;

    @Test
    @DirtiesContext
    void shouldRegisterUserAndCreateActivatingToken() throws Exception {
        //given
        UserRegistrationForm userRegistrationForm = validRegistrationForm();
        //when
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationForm)))
                .andExpect(status().isOk());
        //then
        UserEntity registeredUser = userRepository.findByEmail(userRegistrationForm.getEmail()).get();
        assertEquals(userRegistrationForm.getEmail(), registeredUser.getEmail());
        assertEquals(userRegistrationForm.getUsername(), registeredUser.getUsername());
        assertEquals(Role.USER, registeredUser.getRole());
        assertFalse(registeredUser.isEnabled());
        assertEquals(1, registeredUser.getToken().size());
        assertEquals(TokenType.ACTIVATING, registeredUser.getToken().get(0).getType());
        assertFalse(registeredUser.getToken().get(0).isRevoked());
        assertFalse(registeredUser.getToken().get(0).isExpired());
    }

    @ParameterizedTest
    @MethodSource("duplicatedUserCredentials")
    @Transactional
    void duplicateUserCredentialsTest(UserRegistrationForm userRegistrationForm, UserEntity user, String message) throws Exception {
        //given
        userRepository.save(user);
        //when&then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationForm)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CredentialsTakenException))
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getMessage(), message));
    }

    private static Stream<Arguments> duplicatedUserCredentials() {
        UserRegistrationForm userToSave = validRegistrationForm();
        UserEntity savedUser = UserEntity.builder()
                .username(validRegistrationForm().getUsername())
                .password(userToSave.getPassword())
                .email(userToSave.getEmail())
                .build();

        UserRegistrationForm takenEmail = validRegistrationForm();
        takenEmail.setUsername("123_asdcfg");

        UserRegistrationForm takenUsername = validRegistrationForm();
        takenUsername.setEmail("newemail@tst.pl");

        UserRegistrationForm takenEmailAndUsername = validRegistrationForm();

        return Stream.of(
                arguments(takenEmail, savedUser, "Email is already taken."),
                arguments(takenUsername, savedUser, "Username is already taken."),
                arguments(takenEmailAndUsername, savedUser, "Username and Email are already taken.")
        );
    }
    @ParameterizedTest
    @MethodSource("registrationRequests")
    @Transactional
    void validateRegisterTest(UserRegistrationForm userRegistrationForm) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRegistrationForm)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }



    private static Stream<Arguments> registrationRequests() {
        //CASE 1 TOO SHORT USERNAME
        UserRegistrationForm tooShortUsername = validRegistrationForm();
        tooShortUsername.setUsername("a");

        //CASE 2 SPECIAL CHARACTER IN USERNAME
        UserRegistrationForm usernameWithNotAllowedCharacter = validRegistrationForm();
        usernameWithNotAllowedCharacter.setUsername("asd_asd@");

        //CASE 3 TOO LONG USERNAME
        UserRegistrationForm tooLongUsername = validRegistrationForm();
        tooLongUsername.setUsername("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        //CASE 4 NO NUMBER IN PASSWORD
        UserRegistrationForm noNumberPasswordUser = validRegistrationForm();
        noNumberPasswordUser.setPassword("Abcdefgh*)");

        //CASE 5 NO SPECIAL CHARACTER IN PASSWORD
        UserRegistrationForm noSpecialCharacterPasswordUser = validRegistrationForm();
        noSpecialCharacterPasswordUser.setPassword("Abcdefgh123");

        //CASE 6 NO UPPER CASE LETTER IN PASSWORD
        UserRegistrationForm noUpperCaseLetterPasswordUser = validRegistrationForm();
        noUpperCaseLetterPasswordUser.setPassword("abcdefgh123$");

        //CASE 7 NO LOWER CASE LETTER IN PASSWORD
        UserRegistrationForm noLowerCaseLetterPasswordUser = validRegistrationForm();
        noLowerCaseLetterPasswordUser.setPassword("ABCDEFGH123$");

        //CASE 8 INVALID EMAIL FORMAT
        UserRegistrationForm invalidEmailFormat1 = validRegistrationForm();
        invalidEmailFormat1.setEmail("asd@");
        UserRegistrationForm invalidEmailFormat2 = validRegistrationForm();
        invalidEmailFormat2.setEmail("asd.pl");
        UserRegistrationForm invalidEmailFormat3 = validRegistrationForm();
        invalidEmailFormat3.setEmail("asd@.");

        return Stream.of(
                arguments(tooShortUsername),
                arguments(usernameWithNotAllowedCharacter),
                arguments(tooLongUsername),
                arguments(noNumberPasswordUser),
                arguments(noSpecialCharacterPasswordUser),
                arguments(noUpperCaseLetterPasswordUser),
                arguments(noLowerCaseLetterPasswordUser),
                arguments(invalidEmailFormat1),
                arguments(invalidEmailFormat2),
                arguments(invalidEmailFormat3)
        );
    }

    private static UserRegistrationForm validRegistrationForm() {
        return UserRegistrationForm.builder().email("test@oo.xy").password("aaaaaaB1@").username("user_name").build();
    }

    private String asJsonString(Object obj) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
