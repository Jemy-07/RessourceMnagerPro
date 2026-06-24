package com.cuea.rmp.notification.application.dto;

import com.cuea.rmp.notification.domain.DeviceToken;
import com.cuea.rmp.notification.domain.Platform;

import java.util.UUID;

public record DeviceTokenResult(UUID id, UUID userId, Platform platform) {

    public static DeviceTokenResult from(DeviceToken token) {
        return new DeviceTokenResult(token.getId(), token.getUserId(), token.getPlatform());
    }
}
