package com.example.twitterclone.model

import groovy.transform.Canonical

@Canonical
class Comment {
    String id = UUID.randomUUID().toString()
    String authorId
    String text
    Date createdAt = new Date()
}
