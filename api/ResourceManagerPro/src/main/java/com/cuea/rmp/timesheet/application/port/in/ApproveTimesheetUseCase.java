package com.cuea.rmp.timesheet.application.port.in;

import com.cuea.rmp.timesheet.application.dto.TimesheetResult;

import java.util.UUID;

public interface ApproveTimesheetUseCase {
    TimesheetResult approve(UUID id);
}
