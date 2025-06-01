package com.example.twitterclone.security

import com.example.twitterclone.model.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails implements UserDetails {
    private final User user

    CustomUserDetails(User user) {
        this.user = user
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return []
    }

    @Override
    String getPassword() {
        return user.password
    }

    @Override
    String getUsername() {
        return user.username
    }

    User getUser() {
        return user
    }
}
