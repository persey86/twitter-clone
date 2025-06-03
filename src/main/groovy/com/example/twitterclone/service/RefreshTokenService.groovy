package com.example.twitterclone.service

import com.example.twitterclone.model.RefreshToken
import com.example.twitterclone.model.User
import com.example.twitterclone.repository.RefreshTokenRepository
import com.example.twitterclone.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.time.Instant

@Service
class RefreshTokenService {

    @Value('${jwt.refresh-token-expiration}')
    Long refreshTokenDurationMs

    @Autowired
    RefreshTokenRepository refreshTokenRepository

    @Autowired
    JwtTokenProvider jwtTokenProvider

    RefreshToken createRefreshToken(User user) {
        def token = new RefreshToken(
                userId: user.id,
                token: jwtTokenProvider.generateRefreshToken(user),
                expiryDate: Instant.now().plusMillis(refreshTokenDurationMs)
        )
        refreshTokenRepository.save(token)
    }

    RefreshToken verifyExpiration(RefreshToken token) {
        if (token.expiryDate.isBefore(Instant.now())) {
            refreshTokenRepository.delete(token)
            throw new NoSuchElementException("Refresh token expired.")
        }
        return token
    }

    void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId)
    }

    Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
    }
}
