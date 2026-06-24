package com.cuea.rmp.notification.application.dto;

import com.cuea.rmp.notification.domain.Notification;
import com.cuea.rmp.notification.domain.NotificationType;

import java.util.UUID;

public record NotificationResult(
        UUID id,
        UUID userId,
        NotificationType type,
        String message,
        boolean read
) {
    public static NotificationResult from(Notification notification) {
        return new NotificationResult(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead());
    }
}
