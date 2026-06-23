package com.cuea.rmp.user.application.port.in;

import com.cuea.rmp.user.application.dto.UserResult;

import java.util.UUID;

public interface GetUserUseCase {
    UserResult get(UUID id);
}
