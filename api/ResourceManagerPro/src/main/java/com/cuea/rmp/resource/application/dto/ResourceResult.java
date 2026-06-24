package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.AvailabilityStatus;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.resource.domain.ResourceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResourceResult(
        UUID id,
        UUID orgId,
        UUID userId,
        String name,
        ResourceType type,
        BigDecimal hourlyRateAmount,
        String currency,
        AvailabilityStatus availabilityStatus,
        List<ResourceSkillResult> skills
) {
    public static ResourceResult from(Resource resource) {
        return new ResourceResult(
                resource.getId(),
                resource.getOrgId(),
                resource.getUserId(),
                resource.getName(),
                resource.getType(),
                resource.getHourlyRate().getAmount(),
                resource.getHourlyRate().getCurrency(),
                resource.getAvailabilityStatus(),
                resource.getSkills().stream().map(ResourceSkillResult::from).toList());
    }
}
