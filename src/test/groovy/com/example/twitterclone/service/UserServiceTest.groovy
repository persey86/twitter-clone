package com.example.twitterclone.service

import com.example.twitterclone.model.RefreshToken
import com.example.twitterclone.model.User
import com.example.twitterclone.repository.RefreshTokenRepository
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.TokenResponseDto
import com.example.twitterclone.response.UserRequestDto
import com.example.twitterclone.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

import java.time.Instant

class UserServiceTest extends Specification {
    def userRepository = Mock(UserRepository)
    def jwtTokenProvider = Mock(JwtTokenProvider)
    def passwordEncoder = Mock(PasswordEncoder)
    def authenticationManager = Mock(AuthenticationManager)
    def refreshTokenService = Mock(RefreshTokenService)
    def refreshTokenRepository = Mock(RefreshTokenRepository)
    def userService = new UserService(userRepository, jwtTokenProvider, passwordEncoder, authenticationManager, refreshTokenService, refreshTokenRepository)

    def "register should save new user and return ID"() {
        given:
        def user = new User(username: "test", password: "pass", bio: "test_bio")
        def dto = new UserRequestDto(username: "test", password: "pass", bio: "test_bio")
        userRepository.findByUsername("test") >> Optional.empty()
        userRepository.save(_) >> { User u -> u.id = "123"; return u }
        passwordEncoder.encode("pass") >> "hashedPass"
        jwtTokenProvider.generateToken(_) >> "token"

        when:
        def result = userService.register(dto)

        then:
        result.id == "123"
        result.bio == "test_bio"
    }

    def "login should return tokens for valid credentials"() {
        given:
        def user = new User(id: "1", username: "test", password: "hashedPass")
        def userReqDto = new UserRequestDto(username: "test", password: "hashedPass")
        userRepository.findByUsername("test") >> Optional.of(user)
        passwordEncoder.matches("hashedPass", "hashedPass") >> true
        jwtTokenProvider.generateToken(userReqDto.username) >> "token"
        refreshTokenService.createRefreshToken(user) >> new RefreshToken(token: "refreshToken")

        when:
        def result = userService.logIn(userReqDto)

        then:
        result.getAccessToken() == "token"
        result.getRefreshToken() == "refreshToken"
    }

    def "should invalidate token on logout if header is valid"() {
        given:
        def token = "some.token.value"
        def header = "Bearer $token"
        def refreshToken = "some.refresh.value"
        def tokenServiceResult = new RefreshToken(token: "refreshToken", id: "123")

        refreshTokenService.findByToken(refreshToken) >> Optional.of(tokenServiceResult)

        when:
        userService.logOut(header, refreshToken)

        then:
        1 * jwtTokenProvider.invalidateToken(token)
    }

    def "should not invalidate token if header is null"() {
        given:
        def refreshToken = "some.refresh.value"
        def tokenServiceResult = new RefreshToken(token: "refreshToken", id: "123")

        refreshTokenService.findByToken(refreshToken) >> Optional.of(tokenServiceResult)

        when:
        userService.logOut(null, refreshToken)

        then:
        0 * jwtTokenProvider.invalidateToken(_)
    }

    def "should not invalidate token if header does not start with Bearer"() {
        given:
        def refreshToken = "some.refresh.value"
        def tokenServiceResult = new RefreshToken(token: "refreshToken", id: "123")
        refreshTokenService.findByToken(refreshToken) >> Optional.of(tokenServiceResult)

        when:
        userService.logOut("Token abc.def", refreshToken)

        then:
        0 * jwtTokenProvider.invalidateToken(_)
    }

    def "should update user"() {
        given:
        def existing = new User(id: "1", bio: "oldname", username: "old@example.com")
        def updates = new User(bio: "newname", username: "new@example.com")

        when:
        def result = userService.update("1", updates)

        then:
        1 * userRepository.findById("1") >> Optional.of(existing)
        1 * userRepository.save(existing) >> updates

        result.bio == "newname"
    }

    def "should delete user"() {
        when:
        userService.deleteUserById("1")

        then:
        1 * userRepository.deleteById("1")
    }

    def "should follow another user"() {
        given:
        def user = new User(id: "1", following: [])
        def target = new User(id: "2")

        when:
        def result = userService.follow("1", "2")

        then:
        1 * userRepository.findById("1") >> Optional.of(user)
        1 * userRepository.findById("2") >> Optional.of(target)
        1 * userRepository.save(user) >> user

        result.following.contains("2")
    }

    def "should unfollow a user"() {
        given:
        def user = new User(id: "1", following: ["2"])

        when:
        userService.unfollow("1", "2")

        then:
        1 * userRepository.findById("1") >> Optional.of(user)

        !user.following.contains("2")
    }

    def "should return user by ID"() {
        given:
        def user = new User(id: "1", username: "bob")

        when:
        def result = userService.findById("1")

        then:
        1 * userRepository.findById("1") >> Optional.of(user)
        result.username == "bob"
    }

    def "should refresh tokens when refresh token is valid"() {
        given:
        def refreshTokenStr = "validRefreshToken"
        def userId = "123"
        def user = new User(id: userId, username: "johndoe")
        def storedToken = new RefreshToken(userId: userId, token: refreshTokenStr)

        refreshTokenService.findByToken(refreshTokenStr) >> Optional.of(storedToken)
        refreshTokenService.verifyExpiration(storedToken) >> storedToken
        jwtTokenProvider.getUserFromToken(refreshTokenStr) >> userId
        userRepository.findById(userId) >> Optional.of(user)
        jwtTokenProvider.generateToken(user.username) >> "newAccessToken"
        jwtTokenProvider.generateRefreshToken(user) >> "newRefreshToken"
        jwtTokenProvider.getRefreshTokenExpirationDate("newRefreshToken") >> Instant.now()
        refreshTokenRepository.save(_) >> { it[0] }

        when:
        def result = userService.refreshToken(refreshTokenStr)

        then:
        result instanceof TokenResponseDto
        result.accessToken == "newAccessToken"
        result.refreshToken == "newRefreshToken"
    }

    def "should throw when token not found or expired"() {
        given:
        def refreshTokenStr = "invalid"

        refreshTokenService.findByToken(refreshTokenStr) >> Optional.empty()

        when:
        userService.refreshToken(refreshTokenStr)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Refresh token not found or revoked"
    }

    def "should throw when user not found"() {
        given:
        def refreshTokenStr = "valid"
        def userId = "123"
        def storedToken = new RefreshToken(userId: userId, token: refreshTokenStr)

        refreshTokenService.findByToken(refreshTokenStr) >> Optional.of(storedToken)
        refreshTokenService.verifyExpiration(storedToken) >> storedToken
        jwtTokenProvider.getUserFromToken(refreshTokenStr) >> userId
        userRepository.findById(userId) >> Optional.empty()

        when:
        userService.refreshToken(refreshTokenStr)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "User not found"
    }
}
