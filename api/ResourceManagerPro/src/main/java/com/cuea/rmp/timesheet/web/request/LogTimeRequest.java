package com.cuea.rmp.timesheet.web.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** {@code id} is supplied by the client so offline entries retain their identity. */
public record LogTimeRequest(
        @NotNull(message = "id is required (client-supplied)")
        UUID id,

        @NotNull(message = "resourceId is required")
        UUID resourceId,

        @NotNull(message = "assignmentId is required")
        UUID assignmentId,

        @NotNull(message = "workDate is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate workDate,

        @NotNull(message = "hours is required")
        @Positive(message = "hours must be greater than 0")
        @DecimalMax(value = "24.00", message = "hours must not exceed 24")
        BigDecimal hours
) {}
