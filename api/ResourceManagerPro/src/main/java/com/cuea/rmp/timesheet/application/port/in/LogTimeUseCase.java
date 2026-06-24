package com.cuea.rmp.timesheet.application.port.in;

import com.cuea.rmp.timesheet.application.dto.LogTimeCommand;
import com.cuea.rmp.timesheet.application.dto.TimesheetResult;

public interface LogTimeUseCase {
    TimesheetResult logTime(LogTimeCommand command);
}
