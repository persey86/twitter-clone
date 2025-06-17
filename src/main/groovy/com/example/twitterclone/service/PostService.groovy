package com.example.twitterclone.service

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.repository.PostRepository
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.PostDto
import com.example.twitterclone.security.JwtTokenProvider
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.util.regex.Pattern

@Service
class PostService {
    private PostRepository postRepository
    private UserRepository userRepository
    private MeterRegistry meterRegistry
    private final JwtTokenProvider jwtTokenProvider

    PostService(PostRepository postRepository, UserRepository userRepository, MeterRegistry meterRegistry, JwtTokenProvider jwtTokenProvider) {
        this.postRepository = postRepository
        this.userRepository = userRepository
        this.meterRegistry = meterRegistry
        this.jwtTokenProvider = jwtTokenProvider
    }

    @Transactional
    @CacheEvict(value = "postSearchCache", allEntries = true)
    PostDto createNewPost(Post post, String token) {
        post.createdAt = Instant.now()
        post.authorId = getUserIdFromToken(token)
        def savedPost = postRepository.save(post)
        meterRegistry.counter("posts.created.total").increment()
        return toDto(savedPost)
    }

    private String getUserIdFromToken(String token) {
        if (!token?.startsWith("Bearer ")) {
           throw new IllegalArgumentException("Token has incorrect format")
        }
        def tokenValue = token.substring(7)
        def userNameFromToken = jwtTokenProvider.getUserFromToken(tokenValue)
        def user = userRepository.findByUsername(userNameFromToken).orElseThrow { new UsernameNotFoundException("User not found") }
        return user.id
    }

    @Transactional
    @CacheEvict(value = "postSearchCache", allEntries = true)
    PostDto update(String id, Post updates) {
        def post = postRepository.findById(id).orElseThrow { new IllegalArgumentException("Post not found") }
        post.content = updates.content ?: post.content
        def updatedPost = postRepository.save(post)
        return toDto(updatedPost)
    }

    @Transactional
    @CacheEvict(value = "postSearchCache", allEntries = true)
    void delete(String id) {
        postRepository.deleteById(id)
    }

    @Transactional
    PostDto like(String postId, String token) {
        def userId = getUserIdFromToken(token)
        def updateResult = postRepository.updateAddToLikes(postId, userId)

        if (updateResult.modifiedCount == 0) {
            throw new IllegalStateException("User already liked this post or post not found")
        }

        def updatedPost = postRepository.findById(postId)
                .orElseThrow { new NoSuchElementException("Post not found after update") }

        return toDto(updatedPost)
    }

    @Transactional
    PostDto unlike(String postId, String token) {
        def userId = getUserIdFromToken(token)
        def updateResult = postRepository.updateRemoveFromLikes(postId, userId)
        if (updateResult.modifiedCount == 0) {
            throw new IllegalStateException("User has not liked this post or post not found")
        }
        def updatedPost = postRepository.findById(postId)
                .orElseThrow { new NoSuchElementException("Post not found after update") }

        return toDto(updatedPost)
    }

    @Transactional
    PostDto addComment(String postId, Comment comment) {
        if (!comment?.authorId || !comment?.text) {
            throw new IllegalArgumentException("authorId or text must not be empty")
        }

        def post = postRepository.findById(postId).orElseThrow { new NoSuchElementException("Post not found") }
        comment.createdAt = new Date()
        post.comments.add(comment)
        def savedComment = postRepository.save(post)
        return toDto(savedComment)
    }

    @Transactional
    PostDto deleteComment(String postId, String commentId) {
        def post = postRepository.findById(postId).orElseThrow { new NoSuchElementException("Post not found") }
        post.comments.removeIf { it.id == commentId }
        def result = postRepository.save(post)
        return toDto(result)
    }

    @Transactional(readOnly = true)
    List<PostDto> getUserPosts(String userId) {
        if (userId) {
            List<Post> posts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(Collections.singletonList(userId))
            return toListDto(posts)
        }
        return Collections.EMPTY_LIST
    }

    @Transactional(readOnly = true)
    Optional<PostDto> findById(String id) {
        return postRepository.findById(id)
                .ifPresentOrElse(post -> toDto(post), () -> Optional.empty())
    }

    @Transactional(readOnly = true)
    List<PostDto> getFeed(String userId) {
        def user = userRepository.findById(userId).orElseThrow { new NoSuchElementException("User not found") }
        def authorIds = new ArrayList<>(user.following)
        def posts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(authorIds)
        return toListDto(posts)
    }

    @Transactional(readOnly = true)
    List<Comment> getCommentsByPostId(String postId) {
        def post = postRepository.findById(postId).orElseThrow { new NoSuchElementException("Post not found") }
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
