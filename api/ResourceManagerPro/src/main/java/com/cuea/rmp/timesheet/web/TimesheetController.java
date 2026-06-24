package com.cuea.rmp.timesheet.web;

import com.cuea.rmp.shared.application.ApiResponse;
import com.cuea.rmp.timesheet.application.port.in.ApproveTimesheetUseCase;
import com.cuea.rmp.timesheet.application.port.in.ListTimesheetsUseCase;
import com.cuea.rmp.timesheet.application.port.in.LogTimeUseCase;
import com.cuea.rmp.timesheet.application.port.in.SubmitTimesheetUseCase;
import com.cuea.rmp.timesheet.web.request.LogTimeRequest;
import com.cuea.rmp.timesheet.web.response.TimesheetResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timesheets")
public class TimesheetController {

    private final LogTimeUseCase logTime;
    private final SubmitTimesheetUseCase submitTimesheet;
    private final ApproveTimesheetUseCase approveTimesheet;
    private final ListTimesheetsUseCase listTimesheets;
    private final TimesheetWebMapper mapper;

    public TimesheetController(LogTimeUseCase logTime,
                               SubmitTimesheetUseCase submitTimesheet,
                               ApproveTimesheetUseCase approveTimesheet,
                               ListTimesheetsUseCase listTimesheets,
                               TimesheetWebMapper mapper) {
        this.logTime = logTime;
        this.submitTimesheet = submitTimesheet;
        this.approveTimesheet = approveTimesheet;
        this.listTimesheets = listTimesheets;
        this.mapper = mapper;
    }

    // ---- log / submit: any authenticated user (own entries) ----

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TimesheetResponse>> log(@Valid @RequestBody LogTimeRequest request) {
        TimesheetResponse body = mapper.toResponse(logTime.logTime(mapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body, "Time logged"));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TimesheetResponse> submit(@PathVariable UUID id) {
        return ApiResponse.ok(mapper.toResponse(submitTimesheet.submit(id)), "Timesheet submitted");
    }

    // ---- approve: APPROVER (or ADMIN) ----

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','APPROVER')")
    public ApiResponse<TimesheetResponse> approve(@PathVariable UUID id) {
        return ApiResponse.ok(mapper.toResponse(approveTimesheet.approve(id)), "Timesheet approved");
    }

    // ---- read ----

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<TimesheetResponse>> list(
            @RequestParam UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(listTimesheets.list(resourceId, from, to).stream()
                .map(mapper::toResponse).toList());
    }
}
