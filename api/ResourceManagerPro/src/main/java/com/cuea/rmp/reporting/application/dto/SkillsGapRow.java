package com.cuea.rmp.reporting.application.dto;

import java.util.UUID;

/** Supply of a skill across resources; {@code gap} is true when no resource holds it. */
public record SkillsGapRow(
        UUID skillId,
        String skillName,
        long resourceCount,
        double avgProficiency,
        boolean gap
) {}
