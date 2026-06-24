package com.cuea.rmp.auth.application.port.in;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.LoginCommand;

public interface LoginUseCase {
    AuthTokens login(LoginCommand command);
}
