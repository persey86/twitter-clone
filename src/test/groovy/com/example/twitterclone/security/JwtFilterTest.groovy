package com.example.twitterclone.security

import com.example.twitterclone.model.User
import com.example.twitterclone.service.CustomUserDetailsService
import com.example.twitterclone.service.ExpiredTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification

class JwtFilterTest extends Specification {
    def jwtTokenProvider = Mock(JwtTokenProvider)
    def userDetailsService = Mock(CustomUserDetailsService)
    def expiredTokenService = Mock(ExpiredTokenService)
    def jwtFilter = new JwtFilter(jwtTokenProvider, userDetailsService)

    def "should authenticate valid token"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)
        def authentication = new UsernamePasswordAuthenticationToken("testUser", null, [])
        def user = new User()
        def customUserDetails = new CustomUserDetails(user)
        customUserDetails.authorities >> ["USER"]


        request.getHeader("Authorization") >> "Bearer valid.token"
        jwtTokenProvider.isValidToken("valid.token") >> true
        jwtTokenProvider.extractUsername("valid.token") >> "testuser"
        userDetailsService.loadUserByUsername("testuser") >> new CustomUserDetails(new User())

        when:
        jwtFilter.doFilterInternal(request, response, chain)

        then:
        SecurityContextHolder.context.authentication != null
        SecurityContextHolder.context.authentication.authenticated

        cleanup:
        SecurityContextHolder.clearContext()
    }

    def "should skip filter if no token"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)

        request.getHeader("Authorization") >> null

        when:
        jwtFilter.doFilterInternal(request, response, chain)

        then:
        noExceptionThrown()
        !SecurityContextHolder.context.authentication
    }

    def "should skip filter if token is invalid"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def chain = Mock(FilterChain)

        request.getHeader("Authorization") >> "Bearer invalid.token"
        jwtTokenProvider.isValidToken("invalid.token") >> false

        when:
        jwtFilter.doFilterInternal(request, response, chain)

        then:
        noExceptionThrown()
        !SecurityContextHolder.context.authentication
    }

    def "should return false for blacklisted token"() {
        given:
        def token = jwtTokenProvider.generateToken("testuser")
        expiredTokenService.isTokenExpired(token) >> true

        expect:
        !jwtTokenProvider.isValidToken(token)
    }

    def "should invalidate token and reject it afterwards"() {
        given:
        def token = jwtTokenProvider.generateToken("testuser")

        jwtTokenProvider.invalidateToken(token)
    }
}
