package com.cuea.rmp.timesheet.application.dto;

import com.cuea.rmp.timesheet.domain.Timesheet;
import com.cuea.rmp.timesheet.domain.TimesheetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TimesheetResult(
        UUID id,
        UUID resourceId,
        UUID assignmentId,
        LocalDate workDate,
        BigDecimal hours,
        TimesheetStatus status
) {
    public static TimesheetResult from(Timesheet timesheet) {
        return new TimesheetResult(
                timesheet.getId(),
                timesheet.getResourceId(),
                timesheet.getAssignmentId(),
                timesheet.getWorkDate(),
                timesheet.getHours(),
                timesheet.getStatus());
    }
}
