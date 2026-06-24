package com.cuea.rmp.project.application.dto;

import com.cuea.rmp.project.domain.Assignment;
import com.cuea.rmp.project.domain.AssignmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResult(
        UUID id,
        UUID projectId,
        UUID resourceId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct,
        AssignmentStatus status
) {
    public static AssignmentResult from(Assignment assignment) {
        return new AssignmentResult(
                assignment.getId(),
                assignment.getProjectId(),
                assignment.getResourceId(),
                assignment.getTitle(),
                assignment.getPeriod().start(),
                assignment.getPeriod().end(),
                assignment.getAllocationPct(),
                assignment.getStatus());
    }
}
