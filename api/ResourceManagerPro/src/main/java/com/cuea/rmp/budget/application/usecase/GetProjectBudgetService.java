package com.cuea.rmp.budget.application.usecase;

import com.cuea.rmp.budget.application.dto.BudgetResult;
import com.cuea.rmp.budget.application.port.in.GetProjectBudgetUseCase;
import com.cuea.rmp.budget.application.port.out.BudgetRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProjectBudgetService implements GetProjectBudgetUseCase {

    private final BudgetRepository budgetRepository;

    public GetProjectBudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public BudgetResult get(UUID projectId) {
        return budgetRepository.findByProjectId(projectId)
                .map(BudgetResult::from)
                .orElseThrow(() -> new NotFoundException("No budget for project " + projectId));
    }
}
