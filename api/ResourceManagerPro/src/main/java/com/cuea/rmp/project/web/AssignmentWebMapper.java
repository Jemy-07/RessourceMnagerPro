package com.cuea.rmp.project.web;

import com.cuea.rmp.project.application.dto.AssignResourceCommand;
import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.dto.UpdateAssignmentCommand;
import com.cuea.rmp.project.web.request.AssignResourceRequest;
import com.cuea.rmp.project.web.request.UpdateAssignmentRequest;
import com.cuea.rmp.project.web.response.AssignmentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssignmentWebMapper {

    public AssignResourceCommand toCommand(UUID projectId, AssignResourceRequest request) {
        return new AssignResourceCommand(
                projectId, request.resourceId(), request.title(),
                request.startDate(), request.endDate(), request.allocationPct());
    }

    public UpdateAssignmentCommand toCommand(UUID id, UpdateAssignmentRequest request) {
        return new UpdateAssignmentCommand(
                id, request.startDate(), request.endDate(), request.allocationPct(), request.status());
    }

    public AssignmentResponse toResponse(AssignmentResult result) {
        return new AssignmentResponse(
                result.id(), result.projectId(), result.resourceId(), result.title(),
                result.startDate(), result.endDate(), result.allocationPct(), result.status());
    }
}
