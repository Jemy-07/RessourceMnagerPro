package com.cuea.rmp.notification.web.response;

import com.cuea.rmp.notification.domain.NotificationType;

import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType type,
        String message,
        boolean read
) {}
