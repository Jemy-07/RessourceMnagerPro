package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.AvailabilityVerdict;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityResult(
        UUID resourceId,
        LocalDate from,
        LocalDate to,
        boolean available,
        String reason
) {
    public static AvailabilityResult of(UUID resourceId, LocalDate from, LocalDate to, AvailabilityVerdict verdict) {
        return new AvailabilityResult(resourceId, from, to, verdict.available(), verdict.reason());
    }
}
