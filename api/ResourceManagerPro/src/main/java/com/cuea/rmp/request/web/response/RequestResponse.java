package com.cuea.rmp.request.web.response;

import com.cuea.rmp.request.domain.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RequestResponse(
        UUID id,
        UUID requesterId,
        UUID approverId,
        UUID resourceId,
        UUID projectId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        int allocationPct,
        RequestStatus status,
        String comments,
        Instant decidedAt
) {}
