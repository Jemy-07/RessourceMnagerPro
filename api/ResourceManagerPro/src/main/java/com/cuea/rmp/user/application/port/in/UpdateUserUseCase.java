package com.cuea.rmp.user.application.port.in;

import com.cuea.rmp.user.application.dto.UpdateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;

public interface UpdateUserUseCase {
    UserResult update(UpdateUserCommand command);
}
