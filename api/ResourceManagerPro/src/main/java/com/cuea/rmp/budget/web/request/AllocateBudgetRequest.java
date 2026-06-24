package com.cuea.rmp.budget.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AllocateBudgetRequest(
        @NotNull(message = "totalAmount is required")
        @PositiveOrZero(message = "totalAmount must not be negative")
        BigDecimal totalAmount,

        @NotNull(message = "allocatedAmount is required")
        @PositiveOrZero(message = "allocatedAmount must not be negative")
        BigDecimal allocatedAmount,

        @NotBlank(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
        String currency
) {}
