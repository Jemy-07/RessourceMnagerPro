package com.cuea.rmp.notification.application.port.in;

import com.cuea.rmp.notification.domain.NotificationType;

import java.util.UUID;

/**
 * Published port other features call to notify a user. Persists a notification
 * and pushes it to the user's registered devices. Replaces the M6 stub.
 */
public interface NotificationPort {

    void notify(UUID userId, NotificationType type, String message);
}
