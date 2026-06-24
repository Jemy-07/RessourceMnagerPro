package com.cuea.rmp.reporting.application.port.out;

import java.util.UUID;

/** Raw skill-supply row from the read store. */
public record SkillSupplyData(
        UUID skillId,
        String skillName,
        long resourceCount,
        double avgProficiency
) {}
