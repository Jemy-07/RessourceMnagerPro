package com.cuea.rmp.notification.web;

import com.cuea.rmp.auth.infrastructure.security.CurrentUserProvider;
import com.cuea.rmp.notification.application.dto.DeviceTokenResult;
import com.cuea.rmp.notification.application.dto.RegisterDeviceTokenCommand;
import com.cuea.rmp.notification.application.port.in.RegisterDeviceTokenUseCase;
import com.cuea.rmp.notification.web.request.RegisterDeviceTokenRequest;
import com.cuea.rmp.notification.web.response.DeviceTokenResponse;
import com.cuea.rmp.shared.application.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@PreAuthorize("isAuthenticated()")
public class DeviceController {

    private final RegisterDeviceTokenUseCase registerDeviceToken;
    private final CurrentUserProvider currentUser;

    public DeviceController(RegisterDeviceTokenUseCase registerDeviceToken, CurrentUserProvider currentUser) {
        this.registerDeviceToken = registerDeviceToken;
        this.currentUser = currentUser;
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> register(
            @Valid @RequestBody RegisterDeviceTokenRequest request) {
        DeviceTokenResult result = registerDeviceToken.register(new RegisterDeviceTokenCommand(
                currentUser.currentUserId(), request.fcmToken(), request.platform()));
        DeviceTokenResponse body = new DeviceTokenResponse(result.id(), result.userId(), result.platform());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Device token registered"));
    }
}
