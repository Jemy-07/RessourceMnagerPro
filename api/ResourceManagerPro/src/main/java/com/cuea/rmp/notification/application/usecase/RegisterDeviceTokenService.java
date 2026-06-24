package com.cuea.rmp.notification.application.usecase;

import com.cuea.rmp.notification.application.dto.DeviceTokenResult;
import com.cuea.rmp.notification.application.dto.RegisterDeviceTokenCommand;
import com.cuea.rmp.notification.application.port.in.RegisterDeviceTokenUseCase;
import com.cuea.rmp.notification.application.port.out.DeviceTokenRepository;
import com.cuea.rmp.notification.domain.DeviceToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registers (or re-points) a device token. Idempotent on the token string. */
@Service
@Transactional
public class RegisterDeviceTokenService implements RegisterDeviceTokenUseCase {

    private final DeviceTokenRepository deviceTokenRepository;

    public RegisterDeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public DeviceTokenResult register(RegisterDeviceTokenCommand command) {
        DeviceToken token = deviceTokenRepository.findByFcmToken(command.fcmToken())
                .map(existing -> {
                    existing.reassign(command.userId(), command.platform());
                    return existing;
                })
                .orElseGet(() -> DeviceToken.create(command.userId(), command.fcmToken(), command.platform()));
        return DeviceTokenResult.from(deviceTokenRepository.save(token));
    }
}
