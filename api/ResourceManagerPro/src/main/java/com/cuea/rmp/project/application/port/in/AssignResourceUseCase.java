package com.cuea.rmp.project.application.port.in;

import com.cuea.rmp.project.application.dto.AssignResourceCommand;
import com.cuea.rmp.project.application.dto.AssignmentResult;

public interface AssignResourceUseCase {
    /** Assigns a resource to a project, throwing ConflictException on a booking conflict. */
    AssignmentResult assign(AssignResourceCommand command);
}
