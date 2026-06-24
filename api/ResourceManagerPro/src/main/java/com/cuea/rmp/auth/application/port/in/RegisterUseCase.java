package com.cuea.rmp.auth.application.port.in;

import com.cuea.rmp.auth.application.dto.RegisterCommand;
import com.cuea.rmp.auth.application.dto.RegisterResult;

public interface RegisterUseCase {
    RegisterResult register(RegisterCommand command);
}
