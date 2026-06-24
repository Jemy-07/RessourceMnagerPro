package com.cuea.rmp.request.web;

import com.cuea.rmp.request.application.dto.CreateRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.web.request.CreateRequestRequest;
import com.cuea.rmp.request.web.response.RequestResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestWebMapper {

    public CreateRequestCommand toCommand(UUID requesterId, CreateRequestRequest request) {
        return new CreateRequestCommand(
                requesterId,
                request.resourceId(),
                request.projectId(),
                request.title(),
                request.startDate(),
                request.endDate(),
                request.allocationPct());
    }

    public RequestResponse toResponse(RequestResult result) {
        return new RequestResponse(
                result.id(),
                result.requesterId(),
                result.approverId(),
                result.resourceId(),
                result.projectId(),
                result.title(),
                result.startDate(),
                result.endDate(),
                result.allocationPct(),
                result.status(),
                result.comments(),
                result.decidedAt());
    }
}
