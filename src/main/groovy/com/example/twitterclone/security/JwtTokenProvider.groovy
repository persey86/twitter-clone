package com.example.twitterclone.security


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

@Component
@Slf4j
class JwtTokenProvider {
    @Value("\${jwt.secret}")
    private String jwtSecret

    @Value("\${jwt.expiration}")
    private long jwtExpiration

    private final ExpiredTokenService expiredTokenService

    JwtTokenProvider(ExpiredTokenService expiredTokenService) {
        this.expiredTokenService = expiredTokenService
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.bytes)
    }

    String generateToken(String username) {
        def now = new Date()
        def expiryDate = new Date(now.time + jwtExpiration)

        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact()
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

    String extractUsername(String token) {
        return getClaims(token)
        .getBody()
        .getSubject()
    }

    Jws<Claims> getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
    }

    void invalidateToken(String token) {
        expiredTokenService.invalidateToken(token)
    }

}
