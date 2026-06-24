package com.cuea.rmp.request.application.port.in;

import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.domain.RequestStatus;

import java.util.List;

public interface ListRequestsUseCase {
    /** Lists requests, optionally filtered by {@code status} (null = all). */
    List<RequestResult> list(RequestStatus status);
}
