package com.example.twitterclone.repository

import com.example.twitterclone.model.Post
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

import java.time.Instant

interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByAuthorIdInOrderByCreatedAtDesc(List<String> authorIds)
    List<Post> findByContentRegexIgnoreCaseAndCreatedAtLessThanOrderByCreatedAtDesc(String regex, Instant before, Pageable pageable)
    List<Post> findByContentRegexIgnoreCaseOrderByCreatedAtDesc(String regex, Pageable pageable);
}