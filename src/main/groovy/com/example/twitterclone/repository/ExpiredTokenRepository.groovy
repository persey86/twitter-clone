package com.example.twitterclone.repository

import com.example.twitterclone.model.ExpiredToken
import org.springframework.data.mongodb.repository.MongoRepository

interface ExpiredTokenRepository extends MongoRepository<ExpiredToken, String> {
    boolean existsByToken(String token)

}