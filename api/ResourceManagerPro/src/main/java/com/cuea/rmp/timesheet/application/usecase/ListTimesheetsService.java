package com.cuea.rmp.timesheet.application.usecase;

import com.cuea.rmp.timesheet.application.dto.TimesheetResult;
import com.cuea.rmp.timesheet.application.port.in.ListTimesheetsUseCase;
import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListTimesheetsService implements ListTimesheetsUseCase {

    private final TimesheetRepository timesheetRepository;

    public ListTimesheetsService(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }

    @Override
    public List<TimesheetResult> list(UUID resourceId, LocalDate from, LocalDate to) {
        return timesheetRepository.findByResourceAndDateRange(resourceId, from, to).stream()
                .map(TimesheetResult::from).toList();
    }
}
