package com.cuea.rmp.resource.web.response;

import com.cuea.rmp.resource.domain.ResourceType;

import java.math.BigDecimal;
import java.util.UUID;

public record ResourceMatchResponse(
        UUID resourceId,
        String name,
        ResourceType type,
        int proficiency,
        BigDecimal hourlyRateAmount,
        String currency
) {}
