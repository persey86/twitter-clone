package com.example.twitterclone.security

import com.example.twitterclone.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider
    private final CustomUserDetailsService userDetailsService

    JwtFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider
        this.userDetailsService = userDetailsService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        def authHeader = request.getHeader("Authorization")

        if (authHeader?.startsWith("Bearer ")) {
            def token = authHeader.substring(7)
            if (jwtTokenProvider.isValidToken(token)) {
                def userName = jwtTokenProvider.extractUsername(token)
                def userDetails = userDetailsService.loadUserByUsername(userName)
                def auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))
                SecurityContextHolder.context.authentication = auth
            }
        }

        filterChain.doFilter(request, response)
    }
}
