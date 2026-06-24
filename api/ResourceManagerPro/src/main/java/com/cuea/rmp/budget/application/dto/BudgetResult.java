package com.cuea.rmp.budget.application.dto;

import com.cuea.rmp.budget.domain.Budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResult(
        UUID id,
        UUID projectId,
        String currency,
        BigDecimal totalAmount,
        BigDecimal allocatedAmount,
        BigDecimal spentAmount,
        BigDecimal margin,
        BigDecimal remaining
) {
    public static BudgetResult from(Budget budget) {
        return new BudgetResult(
                budget.getId(),
                budget.getProjectId(),
                budget.getCurrency(),
                budget.getTotalAmount().getAmount(),
                budget.getAllocatedAmount().getAmount(),
                budget.getSpentAmount().getAmount(),
                budget.margin().getAmount(),
                budget.remaining().getAmount());
    }
}
