package com.example.twitterclone.controller

import com.example.twitterclone.model.User
import com.example.twitterclone.response.UserRequestDto

import com.example.twitterclone.response.UserResponseDto
import com.example.twitterclone.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController {
    private final UserService userService

    AuthController(UserService userService) {
        this.userService = userService
    }

    @Operation(summary = "Register user and generate token")
    @PostMapping("/register")
    ResponseEntity<UserResponseDto> register(@RequestBody User user) {
        def response =  userService.register(user)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Token validator")
    @GetMapping("/validate-token")
    ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        return userService.isTokenValid(authorizationHeader) ?
                ResponseEntity.ok("Token is valid") :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token")
    }

    @PostMapping("/login")
    ResponseEntity<UserResponseDto> login(@RequestBody UserRequestDto request) {
        def response = userService.logIn(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    ResponseEntity<?> logOut(@RequestHeader("Authorization") String authHeader) {
        userService.logOut(authHeader)
        return ResponseEntity.ok([message: "Logged out successfully"])
    }
}
