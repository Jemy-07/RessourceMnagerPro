package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.AssignmentResult;

import java.util.UUID;

public interface GetAssignmentUseCase {
    AssignmentResult get(UUID id);
}
