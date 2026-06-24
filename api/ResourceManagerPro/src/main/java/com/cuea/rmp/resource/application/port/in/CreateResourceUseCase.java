package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.CreateResourceCommand;
import com.cuea.rmp.resource.application.dto.ResourceResult;

public interface CreateResourceUseCase {
    ResourceResult create(CreateResourceCommand command);
}
