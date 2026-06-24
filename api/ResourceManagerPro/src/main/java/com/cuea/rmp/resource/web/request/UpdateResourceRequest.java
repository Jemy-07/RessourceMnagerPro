package com.cuea.rmp.resource.web.request;

import com.cuea.rmp.resource.domain.AvailabilityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateResourceRequest(
        @NotBlank(message = "name must not be blank")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,

        @NotNull(message = "hourlyRateAmount is required")
        @PositiveOrZero(message = "hourlyRateAmount must not be negative")
        BigDecimal hourlyRateAmount,

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
        String currency,

        @NotNull(message = "availabilityStatus is required")
        AvailabilityStatus availabilityStatus
) {}
