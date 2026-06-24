package com.cuea.rmp.project.application.dto;

import com.cuea.rmp.project.domain.Project;
import com.cuea.rmp.project.domain.ProjectStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectResult(
        UUID id,
        UUID orgId,
        UUID managerId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {
    public static ProjectResult from(Project project) {
        return new ProjectResult(
                project.getId(),
                project.getOrgId(),
                project.getManagerId(),
                project.getName(),
                project.getDescription(),
                project.getPeriod().start(),
                project.getPeriod().end(),
                project.getStatus());
    }
}
