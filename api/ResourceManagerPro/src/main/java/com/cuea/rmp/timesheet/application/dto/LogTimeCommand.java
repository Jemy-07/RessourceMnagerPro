package com.cuea.rmp.timesheet.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** {@code id} is supplied by the caller so offline entries keep their identity. */
public record LogTimeCommand(
        UUID id,
        UUID resourceId,
        UUID assignmentId,
        LocalDate workDate,
        BigDecimal hours
) {}
