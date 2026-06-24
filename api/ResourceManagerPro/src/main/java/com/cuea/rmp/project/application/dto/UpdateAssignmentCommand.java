package com.cuea.rmp.project.application.dto;

import com.cuea.rmp.project.domain.AssignmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateAssignmentCommand(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct,
        AssignmentStatus status
) {}
