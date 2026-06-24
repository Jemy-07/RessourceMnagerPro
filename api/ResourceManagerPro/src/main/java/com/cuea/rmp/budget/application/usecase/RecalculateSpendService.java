package com.cuea.rmp.budget.application.usecase;

import com.cuea.rmp.budget.application.port.in.RecalculateSpendUseCase;
import com.cuea.rmp.budget.application.port.out.BudgetRepository;
import com.cuea.rmp.budget.domain.Budget;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.project.domain.Assignment;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.shared.domain.Money;
import com.cuea.rmp.timesheet.application.port.out.TimesheetRepository;
import com.cuea.rmp.timesheet.domain.Timesheet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Recomputes a project's spend = Σ (approved-timesheet hours × resource hourlyRate)
 * across all the project's assignments. No-op if the project has no budget.
 * <p>
 * Assumes resource rates share the budget's currency (no FX conversion).
 */
@Service
@Transactional
public class RecalculateSpendService implements RecalculateSpendUseCase {

    private final BudgetRepository budgetRepository;
    private final AssignmentRepository assignmentRepository;
    private final TimesheetRepository timesheetRepository;
    private final ResourceRepository resourceRepository;

    public RecalculateSpendService(BudgetRepository budgetRepository,
                                   AssignmentRepository assignmentRepository,
                                   TimesheetRepository timesheetRepository,
                                   ResourceRepository resourceRepository) {
        this.budgetRepository = budgetRepository;
        this.assignmentRepository = assignmentRepository;
        this.timesheetRepository = timesheetRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void recalculate(UUID projectId) {
        Budget budget = budgetRepository.findByProjectId(projectId).orElse(null);
        if (budget == null) {
            return; // nothing to recalculate
        }

        BigDecimal spend = BigDecimal.ZERO;
        for (Assignment assignment : assignmentRepository.findByProjectId(projectId)) {
            for (Timesheet timesheet : timesheetRepository.findApprovedByAssignmentId(assignment.getId())) {
                BigDecimal rate = resourceRepository.findById(timesheet.getResourceId())
                        .map(r -> r.getHourlyRate().getAmount())
                        .orElse(BigDecimal.ZERO);
                spend = spend.add(timesheet.getHours().multiply(rate));
            }
        }

        budget.recalculateSpend(Money.of(spend, budget.getCurrency()));
        budgetRepository.save(budget);
    }
}
