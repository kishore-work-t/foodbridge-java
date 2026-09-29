package com.foodbridge.service;

import com.foodbridge.dto.NotificationDtos;
import com.foodbridge.entity.Notification;
import com.foodbridge.entity.User;
import com.foodbridge.exception.ResourceNotFoundException;
import com.foodbridge.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDtos.NotificationResponse> getNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationDtos.NotificationResponse::from)
                .toList();
    }

    public Page<NotificationDtos.NotificationResponse> getNotificationsPaged(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationDtos.NotificationResponse::from);
    }

    public List<NotificationDtos.NotificationResponse> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(NotificationDtos.NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public NotificationDtos.NotificationResponse createNotification(User user, String title, String message, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        return NotificationDtos.NotificationResponse.from(notification);
    }

    @Transactional
    public NotificationDtos.NotificationResponse markAsRead(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return NotificationDtos.NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }
}