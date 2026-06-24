package com.cuea.rmp.reporting.application.port.out;

import java.util.UUID;

/** Raw utilisation row from the read store (no derived fields). */
public record ResourceUtilizationData(
        UUID resourceId,
        String resourceName,
        long activeAssignments,
        long allocatedPct
) {}
