package com.cuea.rmp.resource.web.response;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityResponse(
        UUID resourceId,
        LocalDate from,
        LocalDate to,
        boolean available,
        String reason
) {}
