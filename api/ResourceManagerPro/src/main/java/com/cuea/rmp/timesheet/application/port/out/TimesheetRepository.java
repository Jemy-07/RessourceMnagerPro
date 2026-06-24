package com.cuea.rmp.timesheet.application.port.out;

import com.cuea.rmp.timesheet.domain.Timesheet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimesheetRepository {

    Timesheet save(Timesheet timesheet);

    Optional<Timesheet> findById(UUID id);

    boolean existsById(UUID id);

    List<Timesheet> findByResourceAndDateRange(UUID resourceId, LocalDate from, LocalDate to);

    /** Approved timesheets for an assignment (used for budget spend recalculation). */
    List<Timesheet> findApprovedByAssignmentId(UUID assignmentId);
}
