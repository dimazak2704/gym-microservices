package com.dimazak.gym.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found");
        }
        return authentication.getName();
    }

    public void verifyOwnership(String requestedUsername) {
        String currentUsername = getCurrentUsername();
        if (!currentUsername.equals(requestedUsername)) {
            throw new AccessDeniedException(
                    "You do not have permission to access data for user: " + requestedUsername);
        }
    }
}