package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.ResourceType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateResourceCommand(
        UUID orgId,
        UUID userId,
        String name,
        ResourceType type,
        BigDecimal hourlyRateAmount,
        String currency
) {}
