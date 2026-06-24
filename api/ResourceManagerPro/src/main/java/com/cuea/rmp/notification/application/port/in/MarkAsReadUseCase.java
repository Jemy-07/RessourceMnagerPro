package com.cuea.rmp.notification.application.port.in;

import com.cuea.rmp.notification.application.dto.NotificationResult;

import java.util.UUID;

public interface MarkAsReadUseCase {
    /** Marks the caller's own notification read; {@code userId} scopes ownership. */
    NotificationResult markAsRead(UUID id, UUID userId);
}
