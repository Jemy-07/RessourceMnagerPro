package com.cuea.rmp.notification.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.UUID;

/** A registered device push token for a user. */
public class DeviceToken {

    private final UUID id;
    private UUID userId;
    private final String fcmToken;
    private Platform platform;

    private DeviceToken(UUID id, UUID userId, String fcmToken, Platform platform) {
        this.id = id;
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.platform = platform;
    }

    public static DeviceToken create(UUID userId, String fcmToken, Platform platform) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(platform, "platform must not be null");
        if (fcmToken == null || fcmToken.isBlank()) {
            throw new BusinessRuleException("fcmToken must not be blank", "INVALID_DEVICE_TOKEN");
        }
        return new DeviceToken(UUID.randomUUID(), userId, fcmToken.trim(), platform);
    }

    public static DeviceToken reconstitute(UUID id, UUID userId, String fcmToken, Platform platform) {
        return new DeviceToken(Objects.requireNonNull(id), userId, fcmToken, platform);
    }

    /** Re-point an existing token registration to a (possibly different) user/platform. */
    public void reassign(UUID userId, Platform platform) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.platform = Objects.requireNonNull(platform, "platform must not be null");
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public Platform getPlatform() {
        return platform;
    }
}
