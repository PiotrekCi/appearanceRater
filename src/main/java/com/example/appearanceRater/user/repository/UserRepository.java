package com.example.appearanceRater.user.repository;

import com.example.appearanceRater.user.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.email = ?1 OR u.username = ?1")
    public Optional<UserEntity> findByCredentials(String credentials);

    public Optional<UserEntity> findByEmail(String email);
    public Optional<UserEntity> findByUsername(String username);
}
