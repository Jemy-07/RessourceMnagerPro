package com.cuea.rmp.request.application.port.in;

import com.cuea.rmp.request.application.dto.CreateRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;

public interface CreateRequestUseCase {
    RequestResult create(CreateRequestCommand command);
}
