package com.example.appearanceRater.jwt;

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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${application.security.jwt.expiration}")
    private long basicExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    @Value("${application.security.secret.key}")
    private String secretKey;
    public String generateToken(final UserEntity user) {
        return generateToken(user, new HashMap<>(), basicExpiration);
    }

    public String generateRefreshToken(final UserEntity user) {
        return generateToken(user, new HashMap<>(), refreshExpiration);
    }

    public String generateToken(final UserEntity user, final Map<String, Object> extraClaims, final long expiration) {
        return buildToken(user, extraClaims, expiration);
    }

    public String extractSubject(final String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public boolean isExpired(final String token) {
        return Date.from(Instant.now()).after(extractExpiration(token));
    }

    public boolean isTokenValid(final String token, final UserEntity user) {
        return extractSubject(token).equals(user.getEmail()) && !isExpired(token);
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

    private String buildToken(UserEntity user, Map<String, Object> extraClaims, long expiration) {
        return Jwts.builder()
                .addClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
