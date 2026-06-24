package com.cuea.rmp.resource.application.port.in;

import com.cuea.rmp.resource.application.dto.AvailabilityResult;

import java.time.LocalDate;
import java.util.UUID;

public interface CheckAvailabilityUseCase {
    AvailabilityResult check(UUID resourceId, LocalDate from, LocalDate to);
}
