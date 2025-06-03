package com.example.twitterclone.security

import com.example.twitterclone.model.User
import com.example.twitterclone.service.ExpiredTokenService
import groovy.util.logging.Slf4j
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.security.Key
import java.time.Instant

@Component
@Slf4j
class JwtTokenProvider {
    @Value('${jwt.secret}')
    private String jwtSecret

    @Value('${jwt.expiration}')
    private long jwtExpiration

    @Value('${jwt.refresh-token-expiration}')
    long refreshTokenExpiration

    private final ExpiredTokenService expiredTokenService

    JwtTokenProvider(ExpiredTokenService expiredTokenService) {
        this.expiredTokenService = expiredTokenService
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.bytes)
    }

    String generateToken(String userName) {
        return Jwts.builder()
        .setSubject(userName)
        .setIssuedAt(new Date())
        .setExpiration(getExpirationDate(jwtExpiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact()
    }

    String generateRefreshToken(User user) {
        def claims = Jwts.claims().setSubject(user.id)
        claims.put("username", user.username)

        Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(getExpirationDate(refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact()
    }

    private static Date getExpirationDate(long expTime) {
        def expiryDate = new Date(System.currentTimeMillis() + expTime)
        return expiryDate
    }

    boolean isValidToken(String token) {
        if (expiredTokenService.isTokenExpired(token)) {
            return false
        }

        try {
            getClaims(token)
            return true
        } catch (Exception ignored) {
            return false
        }
    }

    def getUserFromToken(String token) {
        def claims = getClaims(token)
        if (claims && claims.getBody()) {
            new IllegalArgumentException("Token not valid")
        }
        return extractUsername(token)
    }

    Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
    }

    String extractUsername(String token) {
        return getClaims(token)
                .getBody()
                .getSubject()
    }

    void invalidateToken(String token) {
        expiredTokenService.invalidateToken(token)
    }

    Instant getRefreshTokenExpirationDate(String token) {
        return getClaims(token)
                .getBody()
                .getExpiration()
                .toInstant()
    }

}
