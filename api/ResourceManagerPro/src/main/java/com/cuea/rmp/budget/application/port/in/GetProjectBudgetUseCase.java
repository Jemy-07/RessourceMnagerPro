package com.cuea.rmp.budget.application.port.in;

import com.cuea.rmp.budget.application.dto.BudgetResult;

import java.util.UUID;

public interface GetProjectBudgetUseCase {
    BudgetResult get(UUID projectId);
}
