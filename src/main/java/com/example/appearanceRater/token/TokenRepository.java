package com.example.appearanceRater.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("""
        SELECT t FROM Token t
        WHERE t.token LIKE ?1 AND t.type = 'ACTIVATING'
    """)
    Optional<Token> findRegistrationToken(String token);

    @Query("""
        SELECT t FROM Token t
        WHERE t.token LIKE ?1 AND t.type = 'RECOVERY'
    """)
    Optional<Token> findRecoveryToken(String token);
    @Query("""
        SELECT t FROM Token t
        WHERE t.token LIKE ?1 AND t.type = 'AUTHENTICATION'
    """)
    Optional<Token> findAuthenticatingToken(String token);

    @Modifying
    @Query("""
        DELETE FROM Token t
        WHERE t.type = 'AUTHENTICATION'
        AND (t.expired = true OR t.revoked = true)
        AND t.user.id = :id
    """)
    void deleteAllInvalidTokens(Integer id);
}