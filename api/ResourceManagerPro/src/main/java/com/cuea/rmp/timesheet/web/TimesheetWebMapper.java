package com.cuea.rmp.timesheet.web;

import com.cuea.rmp.timesheet.application.dto.LogTimeCommand;
import com.cuea.rmp.timesheet.application.dto.TimesheetResult;
import com.cuea.rmp.timesheet.web.request.LogTimeRequest;
import com.cuea.rmp.timesheet.web.response.TimesheetResponse;
import org.springframework.stereotype.Component;

@Component
public class TimesheetWebMapper {

    public LogTimeCommand toCommand(LogTimeRequest request) {
        return new LogTimeCommand(
                request.id(), request.resourceId(), request.assignmentId(),
                request.workDate(), request.hours());
    }

    public TimesheetResponse toResponse(TimesheetResult result) {
        return new TimesheetResponse(
                result.id(), result.resourceId(), result.assignmentId(),
                result.workDate(), result.hours(), result.status());
    }
}
