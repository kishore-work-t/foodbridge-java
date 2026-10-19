package com.foodbridge.dto;

import com.foodbridge.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

public class NotificationDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationResponse {

        private Long id;
        private String title;
        private String message;
        private Notification.NotificationType type;
        private Boolean isRead;
        private LocalDateTime createdAt;

        public static NotificationResponse from(Notification notification) {
            return NotificationResponse.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationMarkReadRequest {

        @Builder.Default
        private Boolean read = true;
    }
}