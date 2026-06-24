package com.cuea.rmp.request.application.dto;

import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.request.domain.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RequestResult(
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
) {
    public static RequestResult from(Request request) {
        return new RequestResult(
                request.getId(),
                request.getRequesterId(),
                request.getApproverId(),
                request.getResourceId(),
                request.getProjectId(),
                request.getTitle(),
                request.getPeriod().start(),
                request.getPeriod().end(),
                request.getAllocationPct(),
                request.getStatus(),
                request.getComments(),
                request.getDecidedAt());
    }
}
