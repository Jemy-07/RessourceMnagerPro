package com.cuea.rmp.budget.infrastructure.recalc;

import com.cuea.rmp.budget.application.port.in.RecalculateSpendUseCase;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.timesheet.application.port.out.BudgetRecalculationPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implements the timesheet feature's {@link BudgetRecalculationPort}: maps an
 * approved timesheet's assignment to its project, then recalculates that project's
 * budget spend.
 */
@Component
public class BudgetRecalculationAdapter implements BudgetRecalculationPort {

    private final AssignmentRepository assignmentRepository;
    private final RecalculateSpendUseCase recalculateSpend;

    public BudgetRecalculationAdapter(AssignmentRepository assignmentRepository,
                                      RecalculateSpendUseCase recalculateSpend) {
        this.assignmentRepository = assignmentRepository;
        this.recalculateSpend = recalculateSpend;
    }

    @Override
    public void recalculateForAssignment(UUID assignmentId) {
        assignmentRepository.findById(assignmentId)
                .ifPresent(assignment -> recalculateSpend.recalculate(assignment.getProjectId()));
    }
}
