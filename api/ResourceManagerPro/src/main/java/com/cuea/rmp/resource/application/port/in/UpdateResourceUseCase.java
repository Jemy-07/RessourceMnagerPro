package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.dto.UpdateResourceCommand;

public interface UpdateResourceUseCase {
    ResourceResult update(UpdateResourceCommand command);
}
