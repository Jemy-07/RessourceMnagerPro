package com.cuea.rmp.timesheet.application.usecase;

import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.timesheet.application.dto.TimesheetResult;
import com.cuea.rmp.timesheet.application.port.in.SubmitTimesheetUseCase;
import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import com.cuea.rmp.timesheet.domain.Timesheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SubmitTimesheetService implements SubmitTimesheetUseCase {

    private final TimesheetRepository timesheetRepository;

    public SubmitTimesheetService(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }

    @Override
    public TimesheetResult submit(UUID id) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Timesheet " + id + " not found"));
        timesheet.submit();
        return TimesheetResult.from(timesheetRepository.save(timesheet));
    }
}
