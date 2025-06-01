package com.example.twitterclone.service

import com.example.twitterclone.model.User
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.UserRequestDto
import com.example.twitterclone.response.UserResponseDto
import com.example.twitterclone.security.JwtTokenProvider
import org.apache.commons.lang3.StringUtils
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {
    private final UserRepository userRepository
    private final JwtTokenProvider jwtTokenProvider
    private final PasswordEncoder passwordEncoder
    private final AuthenticationManager authenticationManager

    UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository
        this.jwtTokenProvider = jwtTokenProvider
        this.passwordEncoder = passwordEncoder
        this.authenticationManager = authenticationManager
    }

    def register(User user) {
        isUserRegistered(user.username)
        // do encode password
        def encodedPassword = passwordEncoder.encode(user.password)
        user.setPassword(encodedPassword)
        // generate token
        def token = jwtTokenProvider.generateToken(user.username)
        userRepository.save(user)

        return toDto(user, token)
    }

    private void isUserRegistered(String username) {
        if (userRepository.findByUsername(username).present) {
            throw IllegalArgumentException("Username already exists")
        }
    }

    def update(String id, User updates) {
        def existingUser = findById(id)
        existingUser.bio = updates.bio ?: existingUser.bio
        existingUser.password = updates.password ?: existingUser.password
        userRepository.save(existingUser)
        return toDto(existingUser)
    }

    void deleteUserById(String id) {
        userRepository.deleteById(id)
    }

    def follow(String userId, String targetId) {
        if (userId == targetId) {
            throw new IllegalArgumentException("Cannot follow yourself")
        }

        def follower = userRepository.findById(userId).orElseThrow { new IllegalArgumentException("Follower not found") }
        def followee = userRepository.findById(targetId).orElseThrow { new IllegalArgumentException("Followee not found") }
        follower.following.add(targetId)
        userRepository.save(follower)
        return toDto(follower)
    }

    def unfollow(String userId, String targetId) {
        def user = findById(userId)
        user.following.remove(targetId)
        userRepository.save(user)
        return toDto(user)
    }

    User findById(String id) {
        return userRepository.findById(id).orElseThrow { new IllegalArgumentException("User not found") }
    }

    def logIn(UserRequestDto request) {
        def userName = request.username
        if (StringUtils.isNoneBlank(userName)) {
            new IllegalArgumentException("Username can't be empty or null!")
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username, request.password))
        def user = userRepository.findByUsername(userName).orElseThrow { new UsernameNotFoundException("User not found") }
        def token = jwtTokenProvider.generateToken(userName)
        return toDto(user, token)
    }

    void logOut(String authHeader) {
        def token = authHeader?.startsWith("Bearer ") ? authHeader.substring(7) : null
        if (token) {
            jwtTokenProvider.invalidateToken(token)
        }
    }

    boolean isTokenValid(String authHeader) {
        def token = authHeader?.substring(7)
        return jwtTokenProvider.isValidToken(token)
    }

    private static UserResponseDto toDto(User user, String token) {
        return UserResponseDto.builder()
                .id(user.id)
                .token(token)
                .bio(user.bio)
                .build()
    }

    private static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.id)
                .bio(user.bio)
                .following(user.following)
                .build()
    }

}
