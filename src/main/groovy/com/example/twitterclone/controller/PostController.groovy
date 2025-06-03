package com.example.twitterclone.controller

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.response.PostDto
import com.example.twitterclone.service.PostService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/posts")
class PostController {
    private final PostService postService

    PostController(PostService postService) {
        this.postService = postService
    }

    @Operation(summary = "Create post")
    @PostMapping
    PostDto createPost(@RequestHeader("Authorization") String authHeader, @RequestBody Post post) {
        return postService.createNewPost(post, authHeader)
    }

    @Operation(summary = "Update post")
    @PutMapping("/{id}")
    PostDto update(@PathVariable("id") String id, @RequestBody Post post) {
        return postService.update(id, post)
    }

    @Operation(summary = "Delete post by id")
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") String id) {
        postService.delete(id)
    }

    @PostMapping("/{postId}/like")
    PostDto like(@RequestHeader("Authorization") String authHeader, @PathVariable("postId") String postId) {
        return postService.like(postId, authHeader)
    }

    @PostMapping("/{id}/unlike/{userId}")
    PostDto unlike(@PathVariable("id") String id, @PathVariable("userId") String userId) {
        return postService.unlike(id, userId)
    }

    @Operation(summary = "Add new comment by post id")
    @PostMapping("/{id}/comment")
    PostDto comment(@PathVariable("id") String id, @RequestBody Comment comment) {
        return postService.addComment(id, comment)
    }

    @Operation(summary = "Delete comment by post id")
    @DeleteMapping("/{postId}/comment/{commentId}")
    PostDto deleteComment(@PathVariable("postId") String postId, @PathVariable("commentId") String commentId) {
        return postService.deleteComment(postId, commentId)
    }

    @Operation(summary = "Get user posts")
    @GetMapping("/user/{userId}")
    List<PostDto> getUserPosts(@PathVariable("userId") String userId) {
        return postService.getUserPosts(userId)
    }

    @GetMapping("/{id}")
    PostDto get(@PathVariable("id") String id) {
        return postService.findById(id).orElseThrow {
            new IllegalArgumentException("Post not found")
        }
    }

    @Operation(summary = "Get user subscriptions")
    @GetMapping("/feed/{userId}")
    List<PostDto> getFeed(@PathVariable("userId") String userId) {
        return postService.getFeed(userId)
    }

    @Operation(summary = "Get all comments by post id")
    @GetMapping("/{id}/comments")
    List<Comment> getComments(@PathVariable("id") String id) {
        return postService.getCommentsByPostId(id)
    }

    @Operation(
            summary = "Search by text from query where 'limit' - num of posts in return, 'afterId' - show records after it"
    )
    @GetMapping(value = "/search", produces = "application/json")
    ResponseEntity<List<PostDto>> searchPosts(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(value = "afterId", required = false) String afterId) {
        def result = postService.searchPosts(query, limit, afterId)
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result)
    }
}