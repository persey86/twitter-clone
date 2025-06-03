package com.example.twitterclone.service

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.model.User
import com.example.twitterclone.repository.PostRepository
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.security.JwtTokenProvider
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.time.Instant
import java.util.regex.Pattern

class PostServiceTest extends Specification {
    PostRepository postRepository
    UserRepository userRepository
    PostService postService
    MeterRegistry meterRegistry
    JwtTokenProvider jwtTokenProvider

    def setup() {
        postRepository = Mock(PostRepository)
        userRepository = Mock(UserRepository)
        meterRegistry = Mock(MeterRegistry)
        jwtTokenProvider = Mock(JwtTokenProvider)
        postService = new PostService(postRepository, userRepository, meterRegistry, jwtTokenProvider)
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

    def "getComments should return null of post if postId not found"() {
        given:
        def postId = "post123"
        def comments = [
                new Comment(id: "c1", authorId: "user1", text: "First", createdAt: new Date()),
                new Comment(id: "c2", authorId: "user2", text: "Second", createdAt: new Date())
        ]
        def post = new Post(id: postId, comments: comments)
        postRepository.findById(postId) >> Optional.ofNullable(post)

        when:
        def result = postService.findById(postId)

        then:
        result == null
    }

    def "increments post.created metric"() {
        given:
        def meterRegistry = new SimpleMeterRegistry()
        def service = new PostService(postRepository, userRepository, meterRegistry, jwtTokenProvider)
        def post = new Post(content: "Hello", authorId: "1", id: "123")
        def token = "some.token.value"
        def header = "Bearer $token"
        def user = new User(username: "test", password: "pass", bio: "test_bio")
        userRepository.findByUsername("token") >> Optional.of(user)
        jwtTokenProvider.getUserFromToken(token) >> "token"
        postRepository.save(post) >> post

        when:
        service.createNewPost(post, header)

        then:
        Math.abs(meterRegistry.counter("posts.created.total").count() - 1.0) < 1e-6
    }

    def "should update post content"() {
        when:
        User user = new User(username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(content: "initial content", authorId: user.id, createdAt: Instant.now())
        def updates = new Post(content: "updated content")
        postRepository.findById(post.id) >> Optional.of(post)
        userRepository.save(user) >> user
        postRepository.save(post) >> post

        def result = postService.update(post.id, updates)

        then:
        result.content == "updated content"
    }

    def "should delete post"() {
        when:
        User user = new User(username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "123", content: "initial content", authorId: user.id, createdAt: Instant.now())
        postRepository.findById(post.id) >> Optional.empty()
        postService.delete(post.id)

        then:
        !postRepository.findById(post.id).isPresent()
    }

    def "should like post with valid token"() {
        given:
        User user = new User(id: "123", username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "456", content: "initial content", authorId: user.id, createdAt: Instant.now())
        def token = "Bearer token"
        postRepository.findById(post.id) >> Optional.of(post)
        jwtTokenProvider.getUserFromToken(token) >> "token"
        userRepository.findByUsername(null) >> Optional.of(user)
        postRepository.save(post) >> post

        when:
        def result = postService.like(post.id, token)

        then:
        result.likes.contains(user.id)
    }

    def "should unlike post"() {
        given:
        User user = new User(id: "123", username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "456", content: "initial content", authorId: user.id, createdAt: Instant.now())
        post.likes.add(user.id)
        postRepository.findById(post.id) >> Optional.of(post)
        postRepository.save(post) >> post

        when:
        def result = postService.unlike(post.id, user.id)

        then:
        !result.likes.contains(user.id)
    }

    def "should add comment to post"() {
        when:
        User user = new User(id: "123", username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "456", content: "initial content", authorId: user.id, createdAt: Instant.now())
        def comment = new Comment(authorId: user.id, text: "Nice post!")
        postRepository.findById(post.id) >> Optional.of(post)
        postRepository.save(post) >> post

        def result = postService.addComment(post.id, comment)

        then:
        result.comments.size() == 1
        result.comments[0].text == "Nice post!"
    }

    def "should delete comment by id"() {
        given:
        User user = new User(username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "456", content: "initial content", authorId: user.id, createdAt: Instant.now())
        def comment = new Comment(id: "comment123", authorId: user.id, text: "To delete", createdAt: new Date())
        post.comments.add(comment)
        postRepository.findById(post.id) >> Optional.of(post)
        postRepository.save(post) >> post

        when:
        def result = postService.deleteComment(post.id, "comment123")

        then:
        result.comments.isEmpty()
    }

    def "should get user posts"() {
        when:
        User user = new User(id: "123", username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(content: "initial content", authorId: user.id, createdAt: Instant.now())
        postRepository.findByAuthorIdInOrderByCreatedAtDesc(Collections.singletonList(user.id)) >> Collections.singletonList(post)
        def result = postService.getUserPosts(user.id)

        then:
        result.size() == 1
        result[0].content == post.content
    }

    def "should throw for malformed token in like()"() {
        when:
        User user = new User(username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(id: "123", content: "initial content", authorId: user.id, createdAt: Instant.now())
        postRepository.findById(post.id) >> Optional.of(post)
        postService.like(post.id, "badtoken")

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw if authorId or text missing in comment"() {
        when:
        User user = new User(username: "testuser", password: "pass", bio: "bio")
        Post post = new Post(content: "initial content", authorId: user.id, createdAt: Instant.now())
        postService.addComment(post.id, new Comment(text: "no author"))

        then:
        thrown(IllegalArgumentException)
    }

    def "should return posts matching query without afterId"() {
        given:
        def query = "test"
        def limit = 2
        def afterId = null
        def pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        def regex = ".*${Pattern.quote(query)}.*"

        def post1 = new Post(id: "1", content: "test post 1", createdAt: Instant.now(), authorId: "u1")
        def post2 = new Post(id: "2", content: "another test post", createdAt: Instant.now().minusSeconds(60), authorId: "u2")
        def posts = [post1, post2]

        when:
        def result = postService.searchPosts(query, limit, afterId)

        then:
        1 * postRepository.findByContentRegexIgnoreCaseOrderByCreatedAtDesc(regex, pageable) >> posts
        result.size() == 2
        result*.content.contains("test post 1")
    }

    def "should return posts matching query after given postId"() {
        given:
        def query = "hello"
        def limit = 3
        def afterId = "99"
        def pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        def regex = ".*${Pattern.quote(query)}.*"

        def afterPost = new Post(id: afterId, content: "something", createdAt: Instant.parse("2025-06-01T12:00:00Z"))
        def post1 = new Post(id: "55", content: "hello there", createdAt: Instant.parse("2025-06-01T11:59:00Z"), authorId: "u3")
        def posts = [post1]

        when:
        def result = postService.searchPosts(query, limit, afterId)

        then:
        1 * postRepository.findById(afterId) >> Optional.of(afterPost)
        1 * postRepository.findByContentRegexIgnoreCaseAndCreatedAtLessThanOrderByCreatedAtDesc(regex, afterPost.createdAt, pageable) >> posts
        result.size() == 1
        result[0].content == "hello there"
    }

    def "should throw exception if afterId not found"() {
        given:
        def query = "missing"
        def limit = 5
        def afterId = "not-found"

        when:
        postService.searchPosts(query, limit, afterId)

        then:
        1 * postRepository.findById(afterId) >> Optional.empty()
        thrown(NoSuchElementException)
    }
}