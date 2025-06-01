package com.example.twitterclone.config


import org.springframework.beans.factory.InitializingBean
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Component

import java.time.Duration

@Component
class MongoTokenExpireConfig implements InitializingBean {
    private final MongoTemplate mongoTemplate

    MongoTokenExpireConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate
    }

    @Override
    void afterPropertiesSet() throws Exception {
        Index index = new Index()
                .on("expiration", Sort.Direction.ASC)
                .expire(Duration.ofMinutes(30))

        mongoTemplate.indexOps("expired_tokens").ensureIndex(index)
    }
}
