package com.foodbridge.dto;

import com.foodbridge.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        private String password;

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        private String phone;

        @NotNull(message = "Role is required")
        private User.Role role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {

        private String token;
        @Builder.Default
        private String type = "Bearer";
        private Long id;
        private String name;
        private String email;
        private User.Role role;
        private User.UserStatus status;

        public AuthResponse(String token, User user) {
            this.token = token;
            this.type = "Bearer";
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.status = user.getStatus();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}