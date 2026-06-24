package com.cuea.rmp.project.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateProjectCommand(
        UUID orgId,
        UUID managerId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {}
