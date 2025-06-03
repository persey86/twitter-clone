package com.example.twitterclone.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.Instant

@Document(collection = "refresh_tokens")
class RefreshToken {
    @Id
    String id
    String userId
    String token
    Instant expiryDate
}
