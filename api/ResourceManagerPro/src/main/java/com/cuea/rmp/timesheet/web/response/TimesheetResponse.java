package com.cuea.rmp.timesheet.web.response;

import com.cuea.rmp.timesheet.domain.TimesheetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TimesheetResponse(
        UUID id,
        UUID resourceId,
        UUID assignmentId,
        LocalDate workDate,
        BigDecimal hours,
        TimesheetStatus status
) {}
