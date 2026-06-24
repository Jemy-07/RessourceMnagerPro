package com.cuea.rmp.notification.web.response;

import com.cuea.rmp.notification.domain.Platform;

import java.util.UUID;

public record DeviceTokenResponse(UUID id, UUID userId, Platform platform) {}
