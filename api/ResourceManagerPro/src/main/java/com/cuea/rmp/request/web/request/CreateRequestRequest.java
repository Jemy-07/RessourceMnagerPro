package com.cuea.rmp.request.web.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRequestRequest(
        @NotNull(message = "resourceId is required")
        UUID resourceId,

        @NotNull(message = "projectId is required")
        UUID projectId,

        @NotBlank(message = "title must not be blank")
        String title,

        @NotNull(message = "startDate is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @NotNull(message = "endDate is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Min(value = 0, message = "allocationPct must be between 0 and 100")
        @Max(value = 100, message = "allocationPct must be between 0 and 100")
        int allocationPct
) {}
