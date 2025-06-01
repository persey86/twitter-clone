package com.example.twitterclone.repository

import com.example.twitterclone.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username)
}
