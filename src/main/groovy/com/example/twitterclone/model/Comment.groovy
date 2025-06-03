package com.example.twitterclone.model

import groovy.transform.Canonical

@Canonical
class Comment implements Serializable {
    private static final long serialVersionUID = 1L

    String id = UUID.randomUUID().toString()
    String authorId
    String text
    Date createdAt = new Date()
}
