package com.cuea.rmp.timesheet.application.usecase;

import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.timesheet.application.dto.TimesheetResult;
import com.cuea.rmp.timesheet.application.port.in.ApproveTimesheetUseCase;
import com.cuea.rmp.timesheet.application.port.out.BudgetRecalculationPort;
import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import com.cuea.rmp.timesheet.domain.Timesheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ApproveTimesheetService implements ApproveTimesheetUseCase {

    private final TimesheetRepository timesheetRepository;
    private final BudgetRecalculationPort budgetRecalculationPort;

    public ApproveTimesheetService(TimesheetRepository timesheetRepository,
                                   BudgetRecalculationPort budgetRecalculationPort) {
        this.timesheetRepository = timesheetRepository;
        this.budgetRecalculationPort = budgetRecalculationPort;
    }

    @Override
    public TimesheetResult approve(UUID id) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Timesheet " + id + " not found"));
        timesheet.approve();
        Timesheet saved = timesheetRepository.save(timesheet);

        // Recalculate the owning project's budget spend (no-op if the project has no budget).
        budgetRecalculationPort.recalculateForAssignment(saved.getAssignmentId());
        return TimesheetResult.from(saved);
    }
}
