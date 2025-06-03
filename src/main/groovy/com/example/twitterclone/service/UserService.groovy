package com.example.twitterclone.service

import com.example.twitterclone.model.User
import com.example.twitterclone.repository.RefreshTokenRepository
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.TokenResponseDto
import com.example.twitterclone.response.UserRequestDto
import com.example.twitterclone.response.UserResponseDto
import com.example.twitterclone.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService {
    private final UserRepository userRepository
    private final JwtTokenProvider jwtTokenProvider
    private final PasswordEncoder passwordEncoder
    private final AuthenticationManager authenticationManager
    private final RefreshTokenService refreshTokenService
    private final RefreshTokenRepository refreshTokenRepository

    UserService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
                AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService,
                RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository
        this.jwtTokenProvider = jwtTokenProvider
        this.passwordEncoder = passwordEncoder
        this.authenticationManager = authenticationManager
        this.refreshTokenService = refreshTokenService
        this.refreshTokenRepository = refreshTokenRepository
    }

    def register(UserRequestDto dto) {
        isUserRegistered(dto.username)
        // do encode password
        def encodedPassword = passwordEncoder.encode(dto.password)
        User user = new User(
                password: encodedPassword,
                username: dto.username,
                bio: dto.bio
        )
        def savedUser = userRepository.save(user)
        return toDto(savedUser)
    }

    private void isUserRegistered(String username) {
        if (userRepository.findByUsername(username).present) {
            throw new IllegalArgumentException("Username already exists")
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

        def follower = userRepository.findById(userId).orElseThrow { new NoSuchElementException("Follower not found") }
        def followee = userRepository.findById(targetId).orElseThrow { new NoSuchElementException("Followee not found") }
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
        return userRepository.findById(id).orElseThrow { new NoSuchElementException("User not found") }
    }

    def logIn(UserRequestDto request) {
        def userName = request.username
        if (!userName?.trim()) {
            throw new IllegalArgumentException("Username can't be empty or null!")
        }

        def user = userRepository.findByUsername(userName).orElseThrow { new NoSuchElementException("User not found") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw new IllegalArgumentException("Invalid password")
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username, request.password))
        def newAccToken = jwtTokenProvider.generateToken(user.username)
        def newRefreshToken = refreshTokenService.createRefreshToken(user)
        return toResponseTokenDto(newAccToken, newRefreshToken.token)
    }

    void logOut(String authHeader, String refreshToken) {
        if (authHeader && authHeader.startsWith("Bearer ")) {
            def token = authHeader.substring(7)
            jwtTokenProvider.invalidateToken(token)
        }

        refreshTokenService.findByToken(refreshToken)
                .ifPresentOrElse(refToken ->
                        refreshTokenService.deleteByUserId(refToken.userId), () -> new NoSuchElementException("Token not found"))
    }

    def refreshToken(String refreshToken) {
        def storedToken = refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService.&verifyExpiration)
        .orElseThrow {new NoSuchElementException("Refresh token not found or revoked")}

        def userId = jwtTokenProvider.getUserFromToken(refreshToken)
        def user = userRepository.findById(userId).orElseThrow { new NoSuchElementException("User not found") }
        def newAccessToken = jwtTokenProvider.generateToken(user.username)
        def newRefreshToken = jwtTokenProvider.generateRefreshToken(user)
        storedToken.token = newRefreshToken
        storedToken.expiryDate = jwtTokenProvider.getRefreshTokenExpirationDate(newRefreshToken)
        refreshTokenRepository.save(storedToken)

        return toResponseTokenDto(newAccessToken, newRefreshToken)
    }

    boolean isTokenValid(String authHeader) {
        def token = authHeader?.substring(7)
        return jwtTokenProvider.isValidToken(token)
    }

    private static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.id)
                .bio(user.bio)
                .following(user.following)
                .build()
    }

    private static TokenResponseDto toResponseTokenDto(String accToken, String refreshToken) {
        return TokenResponseDto.builder()
        .accessToken(accToken)
        .refreshToken(refreshToken)
        .build()
    }
}
