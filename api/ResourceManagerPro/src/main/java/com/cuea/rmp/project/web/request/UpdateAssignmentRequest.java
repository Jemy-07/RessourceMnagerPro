package com.cuea.rmp.project.web.request;

import com.cuea.rmp.project.domain.AssignmentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record UpdateAssignmentRequest(
        @NotNull(message = "startDate is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Min(value = 0, message = "allocationPct must be between 0 and 100")
        @Max(value = 100, message = "allocationPct must be between 0 and 100")
        int allocationPct,

        AssignmentStatus status
) {}
