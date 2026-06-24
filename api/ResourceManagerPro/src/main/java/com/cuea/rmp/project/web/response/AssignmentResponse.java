package com.cuea.rmp.project.web.response;

import com.cuea.rmp.project.domain.AssignmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record AssignmentResponse(
        UUID id,
        UUID projectId,
        UUID resourceId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct,
        AssignmentStatus status
) {}
