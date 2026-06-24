package com.cuea.rmp.timesheet.application.usecase;

import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.timesheet.application.dto.LogTimeCommand;
import com.cuea.rmp.timesheet.application.dto.TimesheetResult;
import com.cuea.rmp.timesheet.application.port.in.LogTimeUseCase;
import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import com.cuea.rmp.timesheet.domain.Timesheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logs a time entry using a caller-supplied id (offline-first). The new entry is
 * DRAFT and persisted with syncStatus PENDING (BaseJpaEntity default). A duplicate
 * id is rejected so a re-sync can be detected.
 */
@Service
@Transactional
public class LogTimeService implements LogTimeUseCase {

    private final TimesheetRepository timesheetRepository;

    public LogTimeService(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }

    @Override
    public TimesheetResult logTime(LogTimeCommand command) {
        if (timesheetRepository.existsById(command.id())) {
            throw new ConflictException("Timesheet " + command.id() + " already exists", "TIMESHEET_EXISTS");
        }
        Timesheet timesheet = Timesheet.create(
                command.id(), command.resourceId(), command.assignmentId(),
                command.workDate(), command.hours());
        return TimesheetResult.from(timesheetRepository.save(timesheet));
    }
}
