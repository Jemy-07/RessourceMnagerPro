package com.cuea.rmp.auth.application.port.in;

import com.cuea.rmp.auth.application.dto.LogoutCommand;

public interface LogoutUseCase {
    void logout(LogoutCommand command);
}
