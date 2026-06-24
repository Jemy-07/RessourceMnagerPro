package com.cuea.rmp.notification.application.dto;

import com.cuea.rmp.notification.domain.Platform;

import java.util.UUID;

public record RegisterDeviceTokenCommand(UUID userId, String fcmToken, Platform platform) {}
