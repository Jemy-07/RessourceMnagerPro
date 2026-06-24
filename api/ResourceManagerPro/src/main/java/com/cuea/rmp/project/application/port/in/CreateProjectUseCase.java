package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.CreateProjectCommand;
import com.cuea.rmp.project.application.dto.ProjectResult;

public interface CreateProjectUseCase {
    ProjectResult create(CreateProjectCommand command);
}
