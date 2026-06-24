package com.cuea.rmp.notification.application.port.in;

import com.cuea.rmp.notification.application.dto.DeviceTokenResult;
import com.cuea.rmp.notification.application.dto.RegisterDeviceTokenCommand;

public interface RegisterDeviceTokenUseCase {
    DeviceTokenResult register(RegisterDeviceTokenCommand command);
}
