package com.cuea.rmp.request.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRequestCommand(
        UUID requesterId,
        UUID resourceId,
        UUID projectId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct
) {}
