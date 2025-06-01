package com.example.twitterclone.service

import com.example.twitterclone.model.ExpiredToken
import com.example.twitterclone.repository.ExpiredTokenRepository
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

@Service
@Slf4j
class ExpiredTokenService {
    private ExpiredTokenRepository expiredTokenRepository

    ExpiredTokenService(ExpiredTokenRepository expiredTokenRepository) {
        this.expiredTokenRepository = expiredTokenRepository
    }

    void invalidateToken(String token) {
        def expiredToken = expiredTokenRepository.save(new ExpiredToken(token: token, expiration: new Date()))
        log.info("Token with id: {} has invalidated", expiredToken.id)
    }

    boolean isTokenExpired(String token){
        return expiredTokenRepository.existsByToken(token)
    }
}
