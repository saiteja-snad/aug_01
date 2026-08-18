package com.quickbite.service;

import com.quickbite.dto.response.NotificationResponse;
import com.quickbite.entity.NotificationType;
import com.quickbite.entity.User;

import java.util.List;

public interface NotificationService {
    void notify(User user, String message, NotificationType type);
    List<NotificationResponse> getMyNotifications(String email);
    void markAsRead(Long notificationId, String email);
}
