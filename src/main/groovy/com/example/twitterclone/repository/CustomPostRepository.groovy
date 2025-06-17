package com.example.twitterclone.repository

import com.mongodb.client.result.UpdateResult

interface CustomPostRepository {
    UpdateResult updateAddToLikes(String postId, String userId)

    UpdateResult updateRemoveFromLikes(String postId, String userId)
}