package com.cuea.rmp.request.application.port.in;

import com.cuea.rmp.request.application.dto.ApproveRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;

public interface ApproveRequestUseCase {
    RequestResult approve(ApproveRequestCommand command);
}
