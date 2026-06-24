package com.cuea.rmp.notification.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.UUID;

/** A user-facing notification. Pure Java. */
public class Notification {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String message;
    private boolean read;

    private Notification(UUID id, UUID userId, NotificationType type, String message, boolean read) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.read = read;
    }

    public static Notification create(UUID userId, NotificationType type, String message) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (message == null || message.isBlank()) {
            throw new BusinessRuleException("Notification message must not be blank", "INVALID_NOTIFICATION");
        }
        return new Notification(UUID.randomUUID(), userId, type, message, false);
    }

    public static Notification reconstitute(UUID id, UUID userId, NotificationType type,
                                            String message, boolean read) {
        return new Notification(Objects.requireNonNull(id), userId, type, message, read);
    }

    public void markAsRead() {
        this.read = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }
}
