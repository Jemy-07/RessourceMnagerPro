package com.cuea.rmp.notification.application.port.out;

import com.cuea.rmp.notification.domain.DeviceToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository {

    DeviceToken save(DeviceToken deviceToken);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByUserId(UUID userId);
}
