package com.cuea.rmp.reporting.application.dto;

import java.util.UUID;

/** One resource's utilisation for the heat-map. {@code level} is derived from {@code allocatedPct}. */
public record UtilizationRow(
        UUID resourceId,
        String resourceName,
        long activeAssignments,
        long allocatedPct,
        String level
) {}
