package com.example.appearanceRater.user.repository;

import com.example.appearanceRater.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("""
        SELECT u FROM UserEntity u WHERE u.email = ?1 OR u.username = ?1
    """)
    Optional<UserEntity> findByCredentials(String credentials);

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.email = ?1
    """)
    boolean existsByEmail(String email);

    @Query("""
        SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.username = ?1
    """)
    boolean existsByUsername(String username);
}
