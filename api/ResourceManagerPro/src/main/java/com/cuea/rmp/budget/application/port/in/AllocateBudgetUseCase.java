package com.cuea.rmp.budget.application.port.in;

import com.cuea.rmp.budget.application.dto.AllocateBudgetCommand;
import com.cuea.rmp.budget.application.dto.BudgetResult;

public interface AllocateBudgetUseCase {
    BudgetResult allocate(AllocateBudgetCommand command);
}
