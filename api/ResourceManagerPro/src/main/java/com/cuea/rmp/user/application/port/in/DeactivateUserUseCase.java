package com.cuea.rmp.user.application.port.in;

import java.util.UUID;

public interface DeactivateUserUseCase {
    void deactivate(UUID id);
}
