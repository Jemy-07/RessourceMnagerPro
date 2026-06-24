package com.cuea.rmp.timesheet.application.port.in;

import com.cuea.rmp.timesheet.application.dto.TimesheetResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListTimesheetsUseCase {
    List<TimesheetResult> list(UUID resourceId, LocalDate from, LocalDate to);
}
