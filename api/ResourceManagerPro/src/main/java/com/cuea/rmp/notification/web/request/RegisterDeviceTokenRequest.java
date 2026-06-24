package com.cuea.rmp.notification.web.request;

import com.cuea.rmp.notification.domain.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDeviceTokenRequest(
        @NotBlank(message = "fcmToken must not be blank")
        @Size(max = 512, message = "fcmToken must be at most 512 characters")
        String fcmToken,

        @NotNull(message = "platform is required")
        Platform platform
) {}
