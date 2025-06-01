package com.example.twitterclone.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
class User {
    @Id
    String id

    @Indexed(unique = true)
    String username
    String password
    String bio
    Set<String> following
}
