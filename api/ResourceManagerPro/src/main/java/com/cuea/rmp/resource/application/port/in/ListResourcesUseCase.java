package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.application.dto.ResourceResult;

public interface ListResourcesUseCase {
    PageResult<ResourceResult> list(int page, int size);
}
