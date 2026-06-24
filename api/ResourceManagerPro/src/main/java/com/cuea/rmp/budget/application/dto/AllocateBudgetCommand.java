package com.cuea.rmp.budget.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocateBudgetCommand(
        UUID projectId,
        BigDecimal totalAmount,
        BigDecimal allocatedAmount,
        String currency
) {}
