package com.example.twitterclone.service

import com.example.twitterclone.model.User
import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.response.UserRequestDto
import com.example.twitterclone.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class UserServiceTest extends Specification {
    def userRepository = Mock(UserRepository)
    def jwtTokenProvider = Mock(JwtTokenProvider)
    def passwordEncoder = Mock(PasswordEncoder)
    def authenticationManager = Mock(AuthenticationManager)
    def userService = new UserService(userRepository, jwtTokenProvider, passwordEncoder, authenticationManager)

    def "register should save new user and return JWT"() {
        given:
        def user = new User(username: "test", password: "pass")
        userRepository.findByUsername("test") >> Optional.empty()
        userRepository.save(_) >> { User u -> u.id = "123"; return u }
        passwordEncoder.encode("pass") >> "hashedPass"
        jwtTokenProvider.generateToken(_) >> "token"

        when:
        def result = userService.register(user)

        then:
        result.token == "token"
        result.id == "123"
    }

    def "login should return token for valid credentials"() {
        given:
        def user = new User(id: "1", username: "test", password: "hashedPass")
        def userReqDto = new UserRequestDto(username: "test", password: "hashedPass")
        userRepository.findByUsername("test") >> Optional.of(user)
        passwordEncoder.matches("pass", "hashedPass") >> true
        jwtTokenProvider.generateToken(userReqDto.username) >> "token"

        when:
        def result = userService.logIn(userReqDto)

        then:
        result.token == "token"
    }

    def "should invalidate token on logout if header is valid"() {
        given:
        def token = "some.token.value"
        def header = "Bearer $token"

        when:
        userService.logOut(header)

        then:
        1 * jwtTokenProvider.invalidateToken(token)
    }

    def "should not invalidate token if header is null"() {
        when:
        userService.logOut(null)

        then:
        0 * jwtTokenProvider.invalidateToken(_)
    }

    def "should not invalidate token if header does not start with Bearer"() {
        when:
        userService.logOut("Token abc.def")

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

}
