package com.cuea.rmp.auth.application.port.in;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.RefreshCommand;

public interface RefreshUseCase {
    AuthTokens refresh(RefreshCommand command);
}
