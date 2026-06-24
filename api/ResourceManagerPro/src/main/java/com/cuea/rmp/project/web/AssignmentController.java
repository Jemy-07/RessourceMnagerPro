package com.cuea.rmp.project.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.project.application.port.in.GetAssignmentUseCase;
import com.cuea.rmp.project.application.port.in.UpdateAssignmentUseCase;
import com.cuea.rmp.project.web.request.UpdateAssignmentRequest;
import com.cuea.rmp.project.web.response.AssignmentResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final GetAssignmentUseCase getAssignment;
    private final UpdateAssignmentUseCase updateAssignment;
    private final AssignmentWebMapper mapper;

    public AssignmentController(GetAssignmentUseCase getAssignment,
                               UpdateAssignmentUseCase updateAssignment,
                               AssignmentWebMapper mapper) {
        this.getAssignment = getAssignment;
        this.updateAssignment = updateAssignment;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AssignmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(mapper.toResponse(getAssignment.get(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<AssignmentResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateAssignmentRequest request) {
        return ApiResponse.ok(mapper.toResponse(updateAssignment.update(mapper.toCommand(id, request))),
                "Assignment updated");
    }
}
