package com.cuea.rmp.reporting.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/** Raw project-cost row from the read store. */
public record ProjectCostData(
        UUID projectId,
        String projectName,
        String currency,
        BigDecimal totalAmount,
        BigDecimal allocatedAmount,
        BigDecimal spentAmount
) {}
