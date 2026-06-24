package com.cuea.rmp.project.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AssignResourceCommand(
        UUID projectId,
        UUID resourceId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct
) {}
