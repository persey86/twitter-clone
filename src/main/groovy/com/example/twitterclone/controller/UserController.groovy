package com.example.twitterclone.controller

import com.example.twitterclone.model.User
import com.example.twitterclone.response.UserResponseDto
import com.example.twitterclone.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController {
    private UserService userService

    UserController(UserService userService) {
        this.userService = userService
    }

    @Operation(summary = "Update user's profile")
    @PutMapping("/{id}")
    def update(@PathVariable("id") String id, @RequestBody User user) {
        return userService.update(id, user)
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") String id) {
        userService.deleteUserById(id)
    }

    @Operation(summary = "User can follow to other user by targetId")
    @PostMapping("/{followerId}/follow/{followeeId}")
    def follow(@PathVariable("followerId") String followerId, @PathVariable("followeeId") String followeeId) {
        return userService.follow(followerId, followeeId)
    }

    @Operation(summary = "User can unfollow to other user by targetId")
    @PostMapping("/{followerId}/unfollow/{followeeId}")
    def unfollow(@PathVariable("followerId") String followerId, @PathVariable("followeeId") String followeeId) {
        return userService.unfollow(followerId, followeeId)
    }

    @Operation(summary = "Get User's profile")
    @GetMapping("/{name}")
    UserResponseDto get(@PathVariable("name") String userName) {
        return userService.findByUserName(userName)
    }

}
