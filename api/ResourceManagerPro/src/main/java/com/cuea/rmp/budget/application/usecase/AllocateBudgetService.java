package com.cuea.rmp.budget.application.usecase;

import com.cuea.rmp.budget.application.dto.AllocateBudgetCommand;
import com.cuea.rmp.budget.application.dto.BudgetResult;
import com.cuea.rmp.budget.application.port.in.AllocateBudgetUseCase;
import com.cuea.rmp.budget.application.port.out.BudgetRepository;
import com.cuea.rmp.budget.domain.Budget;
import com.cuea.rmp.shared.domain.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sets a project's total and allocated budget (creating the budget if absent). */
@Service
@Transactional
public class AllocateBudgetService implements AllocateBudgetUseCase {

    private final BudgetRepository budgetRepository;

    public AllocateBudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public BudgetResult allocate(AllocateBudgetCommand command) {
        Money total = Money.of(command.totalAmount(), command.currency());
        Money allocated = Money.of(command.allocatedAmount(), command.currency());

        Budget budget = budgetRepository.findByProjectId(command.projectId())
                .orElseGet(() -> Budget.create(command.projectId(), total));
        budget.allocate(total, allocated);
        return BudgetResult.from(budgetRepository.save(budget));
    }
}
