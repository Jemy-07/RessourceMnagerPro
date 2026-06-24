package com.cuea.rmp.budget.application.port.out;

import com.cuea.rmp.budget.domain.Budget;

import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {

    Budget save(Budget budget);

    Optional<Budget> findByProjectId(UUID projectId);
}
