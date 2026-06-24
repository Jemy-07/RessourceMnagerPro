package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.ResourceType;

import java.math.BigDecimal;
import java.util.UUID;

/** A candidate returned by resource matching, carrying its proficiency for the requested skill. */
public record ResourceMatchResult(
        UUID resourceId,
        String name,
        ResourceType type,
        int proficiency,
        BigDecimal hourlyRateAmount,
        String currency
) {}
