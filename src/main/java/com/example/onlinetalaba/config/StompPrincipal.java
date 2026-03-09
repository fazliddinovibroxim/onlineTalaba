package com.example.onlinetalaba.config;

import com.example.onlinetalaba.entity.User;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final User user;

    public StompPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }
}