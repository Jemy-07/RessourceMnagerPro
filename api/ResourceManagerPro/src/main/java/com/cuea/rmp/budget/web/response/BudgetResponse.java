package com.cuea.rmp.budget.web.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID projectId,
        String currency,
        BigDecimal totalAmount,
        BigDecimal allocatedAmount,
        BigDecimal spentAmount,
        BigDecimal margin,
        BigDecimal remaining
) {}
