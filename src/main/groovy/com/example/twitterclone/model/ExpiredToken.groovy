package com.example.twitterclone.model


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "expired_tokens")
class ExpiredToken {
    @Id
    String id
    String token
    Date expiration
}
