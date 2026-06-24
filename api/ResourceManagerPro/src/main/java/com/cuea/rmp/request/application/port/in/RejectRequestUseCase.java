package com.cuea.rmp.request.application.port.in;

import com.cuea.rmp.request.application.dto.RejectRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;

public interface RejectRequestUseCase {
    RequestResult reject(RejectRequestCommand command);
}
