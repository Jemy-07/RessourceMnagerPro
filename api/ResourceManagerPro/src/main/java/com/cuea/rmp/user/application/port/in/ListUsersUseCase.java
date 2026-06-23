package com.cuea.rmp.user.application.port.in;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.application.dto.UserResult;

public interface ListUsersUseCase {
    PageResult<UserResult> list(int page, int size);
}
