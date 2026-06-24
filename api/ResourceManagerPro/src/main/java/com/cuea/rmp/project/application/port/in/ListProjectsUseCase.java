package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.application.dto.ProjectResult;

public interface ListProjectsUseCase {
    PageResult<ProjectResult> list(int page, int size);
}
