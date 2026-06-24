package com.cuea.rmp.notification.application.port.in;

import com.cuea.rmp.notification.application.dto.NotificationResult;

import java.util.List;
import java.util.UUID;

public interface ListNotificationsUseCase {
    List<NotificationResult> list(UUID userId);
}
