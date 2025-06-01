package com.example.twitterclone.response

import com.example.twitterclone.model.Comment
import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

import java.time.Instant

@ToString
@TupleConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class PostDto implements Serializable {
    private static final long serialVersionUID = 1L

    String id
    String content
    String authorId
    Instant createdAt
    Set<String> likes = []
    List<Comment> comments = []
}
