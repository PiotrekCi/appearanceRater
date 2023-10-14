package com.example.appearanceRater.user.repository;

import com.example.appearanceRater.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
