package com.foodbridge.dto;

import com.foodbridge.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class UserDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {

        private Long id;
        private String name;
        private String email;
        private String phone;
        private User.Role role;
        private User.UserStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserUpdateRequest {

        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserStatusUpdateRequest {

        @NotNull(message = "Status is required")
        private User.UserStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasswordChangeRequest {

        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        private String newPassword;
    }
}