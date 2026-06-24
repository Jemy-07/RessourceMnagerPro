package com.cuea.rmp.reporting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Cost & margin for one project (from its budget). */
public record CostRow(
        UUID projectId,
        String projectName,
        String currency,
        BigDecimal totalAmount,
        BigDecimal allocatedAmount,
        BigDecimal spentAmount,
        BigDecimal margin,
        double marginPct
) {}
