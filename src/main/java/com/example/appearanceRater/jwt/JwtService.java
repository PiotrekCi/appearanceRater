package com.example.appearanceRater.jwt;

import com.example.appearanceRater.token.Token;
import com.example.appearanceRater.user.UserEntity;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final long expiration = 3600000L;
    public String generateToken(UserEntity user) {
        return generateToken(user, new HashMap<>());
    }

    public String generateToken(UserEntity user, Map<String, Object> extraClaims) {
        return buildToken(user, extraClaims);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public boolean isExpired(Token token) {
        return Date.from(Instant.now()).after(extractExpiration(token.getToken()));
    }

    private <T> T extractClaim(String token, Function<Claims, T> getClaim) {
        return getClaim.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(UserEntity user, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .addClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode("DELETED FOR REPO");
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
