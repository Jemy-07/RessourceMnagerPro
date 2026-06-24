package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.ResourceResult;

import java.util.UUID;

public interface GetResourceUseCase {
    ResourceResult get(UUID id);
}
