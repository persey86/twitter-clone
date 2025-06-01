package com.example.twitterclone.service

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.repository.PostRepository
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.PostDto
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

import java.time.Instant
import java.util.regex.Pattern

@Service
class PostService {
    private PostRepository postRepository
    private UserRepository userRepository
    private MeterRegistry meterRegistry

    PostService(PostRepository postRepository, UserRepository userRepository, MeterRegistry meterRegistry) {
        this.postRepository = postRepository
        this.userRepository = userRepository
        this.meterRegistry = meterRegistry
    }

    @CacheEvict(value = "postSearchCache", allEntries = true)
    PostDto createNewPost(Post post) {
        post.createdAt = Instant.now()
        def savedPost = postRepository.save(post)
        meterRegistry.counter("posts.created.total").increment()
        return toDto(savedPost)
    }

    @CacheEvict(value = "postSearchCache", allEntries = true)
    PostDto update(String id, Post updates) {
        def post = postRepository.findById(id).orElseThrow { new IllegalArgumentException("Post not found") }
        post.content = updates.content ?: post.content
        def updatedPost = postRepository.save(post)
        return toDto(updatedPost)
    }

    @CacheEvict(value = "postSearchCache", allEntries = true)
    void delete(String id) {
        postRepository.deleteById(id)
    }

    PostDto like(String postId, String userId) {
        def post = postRepository.findById(postId).orElseThrow { new IllegalArgumentException("Post not found") }
        post.likes.add(userId)
        def likes = postRepository.save(post)

        return toDto(likes)
    }

    PostDto unlike(String postId, String userId) {
        def post = postRepository.findById(postId).orElseThrow { new IllegalArgumentException("Post not found") }
        post.likes.remove(userId)
        def unlike = postRepository.save(post)
        return toDto(unlike)
    }

    PostDto addComment(String postId, Comment comment) {
        if (!comment?.authorId || !comment?.text) {
            throw new IllegalArgumentException("authorId and text must not be empty")
        }

        def post = postRepository.findById(postId).orElseThrow { new IllegalArgumentException("Post not found") }
        comment.createdAt = new Date()
        post.comments.add(comment)
        def savedComment = postRepository.save(post)
        return toDto(savedComment)
    }

    PostDto deleteComment(String postId, String commentId) {
        def post = postRepository.findById(postId).orElseThrow { new IllegalArgumentException("Post not found") }
        post.comments.removeIf { it.id == commentId }
        def result = postRepository.save(post)
        return toDto(result)
    }

    List<PostDto> getUserPosts(String userId) {
        List<Post> posts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(Collections.singletonList(userId))
        return toListDto(posts)
    }

    Optional<Post> findById(String id) {
        return postRepository.findById(id)
    }

    List<PostDto> getFeed(String userId) {
        def user = userRepository.findById(userId).orElseThrow { new IllegalArgumentException("User not found") }
        def authorIds = new ArrayList<>(user.following)
        def posts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(authorIds)
        return toListDto(posts)
    }

    List<Comment> getCommentsByPostId(String postId) {
        def post = findById(postId).orElseThrow { new IllegalArgumentException("Post not found") }
        return post.comments
    }

    @Cacheable(value = "postSearchCache", key = "#query + '_' + #limit + '_' + #afterId")
    List<PostDto> searchPosts(String query, int limit, String afterId) {
        def pageable = PageRequest.of(0, limit ?: 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        def searchText = ".*${Pattern.quote(query)}.*"
        if (afterId) {
            def afterPost = postRepository.findById(afterId).orElseThrow()
            def result = postRepository.findByContentRegexIgnoreCaseAndCreatedAtLessThanOrderByCreatedAtDesc(searchText, afterPost.createdAt, pageable)
            return toListDto(result)
        } else {
            def result = postRepository.findByContentRegexIgnoreCaseOrderByCreatedAtDesc(searchText, pageable)
            return toListDto(result)
        }
    }

    private static List<PostDto> toListDto(List<Post> posts) {
        return posts.collect {post -> toDto(post)}
    }

    private static PostDto toDto(Post post) {
        return PostDto.builder()
        .id(post.id)
        .authorId(post.authorId)
        .content(post.content)
        .createdAt(post.createdAt)
        .comments(post.comments)
        .likes(post.likes)
        .build()
    }

}
