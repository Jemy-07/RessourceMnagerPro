package com.cuea.rmp.user.application.port.in;

import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;

public interface CreateUserUseCase {
    UserResult create(CreateUserCommand command);
}
