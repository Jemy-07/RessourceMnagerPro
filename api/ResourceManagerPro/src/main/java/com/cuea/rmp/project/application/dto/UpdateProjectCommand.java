package com.cuea.rmp.project.application.dto;

import com.cuea.rmp.project.domain.ProjectStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateProjectCommand(
        UUID id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {}
