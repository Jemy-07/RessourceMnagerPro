package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.ProjectResult;

import java.util.UUID;

public interface GetProjectUseCase {
    ProjectResult get(UUID id);
}
