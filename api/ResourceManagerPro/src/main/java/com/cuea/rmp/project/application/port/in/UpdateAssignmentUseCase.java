package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.dto.UpdateAssignmentCommand;

public interface UpdateAssignmentUseCase {
    AssignmentResult update(UpdateAssignmentCommand command);
}
