package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.AssignmentResult;

import java.util.List;
import java.util.UUID;

public interface ListProjectAssignmentsUseCase {
    List<AssignmentResult> listByProject(UUID projectId);
}
