package com.cuea.rmp.resource.web.response;

import com.cuea.rmp.resource.domain.AvailabilityStatus;
import com.cuea.rmp.resource.domain.ResourceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResourceResponse(
        UUID id,
        UUID orgId,
        UUID userId,
        String name,
        ResourceType type,
        BigDecimal hourlyRateAmount,
        String currency,
        AvailabilityStatus availabilityStatus,
        List<ResourceSkillResponse> skills
) {}
