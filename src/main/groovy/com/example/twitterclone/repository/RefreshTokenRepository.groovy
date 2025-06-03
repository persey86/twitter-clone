package com.example.twitterclone.repository

import com.example.twitterclone.model.RefreshToken
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token)
    void deleteByUserId(String userId)

}