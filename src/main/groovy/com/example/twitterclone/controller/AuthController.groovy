package com.example.twitterclone.controller


import com.example.twitterclone.response.RefreshRequest
import com.example.twitterclone.response.TokenResponseDto
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
    ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto user) {
        def response =  userService.register(user)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    ResponseEntity<TokenResponseDto> refreshToken(@RequestBody RefreshRequest request) {
        def response = userService.refreshToken(request.refreshToken)
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
    ResponseEntity<TokenResponseDto> login(@RequestBody UserRequestDto request) {
        def response = userService.logIn(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    ResponseEntity<?> logOut(@RequestHeader("Authorization") String authHeader,
                             @RequestBody RefreshRequest request) {
        if (!authHeader?.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        try {
            userService.logOut(authHeader, request.refreshToken)
            return ResponseEntity.ok([message: "Logged out successfully"])
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}
