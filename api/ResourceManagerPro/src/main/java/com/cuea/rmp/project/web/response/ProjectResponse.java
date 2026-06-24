package com.cuea.rmp.project.web.response;

import com.cuea.rmp.project.domain.ProjectStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID orgId,
        UUID managerId,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {}
