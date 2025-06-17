package com.example.twitterclone.repository

import com.example.twitterclone.model.Post
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class CustomPostRepositoryImpl implements CustomPostRepository {

    private final MongoTemplate mongoTemplate

    CustomPostRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate
    }

    @Override
    UpdateResult updateAddToLikes(String postId, String userId) {
        def query = new Query(Criteria.where("_id").is(postId))
        def update = new Update().addToSet("likes", userId)
        return mongoTemplate.updateFirst(query, update, Post)
    }

    @Override
    UpdateResult updateRemoveFromLikes(String postId, String userId) {
        def query = new Query(Criteria.where("_id").is(postId))
        def update = new Update().pull("likes", userId)
        return mongoTemplate.updateFirst(query, update, Post)
    }
}
