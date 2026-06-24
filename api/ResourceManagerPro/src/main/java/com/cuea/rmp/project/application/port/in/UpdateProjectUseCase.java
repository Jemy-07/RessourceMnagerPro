package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.dto.UpdateProjectCommand;

public interface UpdateProjectUseCase {
    ProjectResult update(UpdateProjectCommand command);
}
