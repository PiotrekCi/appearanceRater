package com.example.appearanceRater.user;

import com.example.appearanceRater.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    public void changePassword(ChangePasswordRequest changePasswordRequest, Principal connectedUser) {
        UserEntity userEntity = (UserEntity) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), userEntity.getPassword())) {
            throw new IllegalStateException("Incorrect password");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())) {
            throw new IllegalStateException("Different passwords provided");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), userEntity.getPassword())) {
            throw new IllegalStateException("New password cannot be the same as old password");
        }

        userEntity.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        repository.save(userEntity);
    }
}
