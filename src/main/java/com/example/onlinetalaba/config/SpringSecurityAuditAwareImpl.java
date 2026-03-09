package com.example.onlinetalaba.config;

import com.example.onlinetalaba.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SpringSecurityAuditAwareImpl implements AuditorAware<User> {
    @Override
    public Optional<User> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user && !authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of((user));
        }
        return Optional.empty();
    }
}
