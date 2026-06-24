package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.AvailabilityStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateResourceCommand(
        UUID id,
        String name,
        BigDecimal hourlyRateAmount,
        String currency,
        AvailabilityStatus availabilityStatus
) {}
