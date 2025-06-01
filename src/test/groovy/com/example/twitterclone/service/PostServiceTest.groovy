package com.example.twitterclone.service

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.model.User
import com.example.twitterclone.repository.PostRepository
import com.example.twitterclone.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import spock.lang.Specification

import java.time.Instant

class PostServiceTest extends Specification {
    PostRepository postRepository
    UserRepository userRepository
    PostService postService
    MeterRegistry meterRegistry

    def setup() {
        postRepository = Mock(PostRepository)
        userRepository = Mock(UserRepository)
        meterRegistry = Mock(MeterRegistry)
        postService = new PostService(postRepository, userRepository, meterRegistry)
    }

    def "getFeed should return posts from followed users ordered by date"() {
        given:
        def userId = "user123"
        List<String> followedUserIds = ["userA", "userB"]

        def user = new User(id: userId, following: followedUserIds)

        def post1 = new Post(id: "p1", authorId: "userA", content: "Post A1", createdAt: Instant.now().minusSeconds(60))
        def post2 = new Post(id: "p2", authorId: "userB", content: "Post B1", createdAt: Instant.now().minusSeconds(30))
        def post3 = new Post(id: "p3", authorId: "userA", content: "Post A2", createdAt: Instant.now())

        when:
        def result = postService.getFeed(userId)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * postRepository.findByAuthorIdInOrderByCreatedAtDesc(followedUserIds) >> [post3, post2, post1]
        result*.id == ["p3", "p2", "p1"]
    }

    def "getFeed should return empty list when user has no subscriptions"() {
        given:
        def userId = "user456"
        def user = new User(id: userId, following: [])

        when:
        def result = postService.getFeed(userId)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * postRepository.findByAuthorIdInOrderByCreatedAtDesc([]) >> []
        result.isEmpty()
    }

    def "getComments should return comments of post"() {
        given:
        def postId = "post123"
        def comments = [
                new Comment(id: "c1", authorId: "user1", text: "First", createdAt: new Date()),
                new Comment(id: "c2", authorId: "user2", text: "Second", createdAt: new Date())
        ]
        def post = new Post(id: postId, comments: comments)

        when:
        def result = postService.findById(postId).get().comments

        then:
        1 * postRepository.findById(postId) >> Optional.of(post)
        result*.id == ["c1", "c2"]
    }

    def "increments post.created metric"() {
        given:
        def meterRegistry = new SimpleMeterRegistry()
        def service = new PostService(postRepository, userRepository, meterRegistry)
        def post = new Post(content: "Hello", authorId: "1", id: "123")
        postRepository.save(post) >> post

        when:
        service.createNewPost(post)

        then:
        Math.abs(meterRegistry.counter("posts.created.total").count() - 1.0) < 1e-6
    }

}