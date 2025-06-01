package com.example.twitterclone.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.Instant

@Document(collection = "posts")
class Post {
    @Id
    String id
    String authorId
    String content
    Instant createdAt
    Set<String> likes = []
    List<Comment> comments = []
}
