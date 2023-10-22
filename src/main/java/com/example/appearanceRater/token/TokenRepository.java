package com.example.appearanceRater.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("""
        SELECT t FROM Token t
        WHERE t.token LIKE ?1 AND t.type = 'ACTIVATING'
    """)
    Optional<Token> findRegistrationToken(String token);
}