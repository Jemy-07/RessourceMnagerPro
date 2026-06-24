package com.cuea.rmp.project.web;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.application.dto.CreateProjectCommand;
import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.dto.UpdateProjectCommand;
import com.cuea.rmp.project.web.request.CreateProjectRequest;
import com.cuea.rmp.project.web.request.UpdateProjectRequest;
import com.cuea.rmp.project.web.response.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProjectWebMapper {

    public CreateProjectCommand toCommand(CreateProjectRequest request) {
        return new CreateProjectCommand(
                request.orgId(), request.managerId(), request.name(),
                request.description(), request.startDate(), request.endDate());
    }

    public UpdateProjectCommand toCommand(UUID id, UpdateProjectRequest request) {
        return new UpdateProjectCommand(
                id, request.name(), request.description(),
                request.startDate(), request.endDate(), request.status());
    }

    public ProjectResponse toResponse(ProjectResult result) {
        return new ProjectResponse(
                result.id(), result.orgId(), result.managerId(), result.name(),
                result.description(), result.startDate(), result.endDate(), result.status());
    }

    public PageResult<ProjectResponse> toResponse(PageResult<ProjectResult> page) {
        return new PageResult<>(
                page.content().stream().map(this::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }
}
