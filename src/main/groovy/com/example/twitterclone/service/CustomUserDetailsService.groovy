package com.example.twitterclone.service

import com.example.twitterclone.repository.UserRepository
import com.example.twitterclone.security.CustomUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository

    CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        def user = userRepository.findByUsername(username).orElseThrow {new UsernameNotFoundException("User not found")}
        return new CustomUserDetails(user)
    }
}
