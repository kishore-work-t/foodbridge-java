package com.foodbridge.security;

import com.foodbridge.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    public boolean isOwner(Long userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId().equals(userId) || currentUser.getRole() == User.Role.ADMIN;
    }
}