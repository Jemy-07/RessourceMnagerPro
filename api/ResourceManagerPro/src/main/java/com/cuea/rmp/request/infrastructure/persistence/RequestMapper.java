package com.cuea.rmp.request.infrastructure.persistence;

import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public Request toDomain(RequestJpaEntity entity) {
        return Request.reconstitute(
                entity.getId(),
                entity.getRequesterId(),
                entity.getApproverId(),
                entity.getResourceId(),
                entity.getProjectId(),
                entity.getTitle(),
                DateRange.of(entity.getStartDate(), entity.getEndDate()),
                entity.getAllocationPct(),
                entity.getStatus(),
                entity.getComments(),
                entity.getDecidedAt());
    }

    public RequestJpaEntity toNewEntity(Request request) {
        RequestJpaEntity entity = new RequestJpaEntity();
        entity.setId(request.getId());
        entity.setRequesterId(request.getRequesterId());
        entity.setResourceId(request.getResourceId());
        entity.setProjectId(request.getProjectId());
        entity.setTitle(request.getTitle());
        entity.setStartDate(request.getPeriod().start());
        entity.setEndDate(request.getPeriod().end());
        entity.setAllocationPct(request.getAllocationPct());
        copyDecisionState(entity, request);
        return entity;
    }

    public void updateEntity(RequestJpaEntity entity, Request request) {
        copyDecisionState(entity, request);
    }

    private void copyDecisionState(RequestJpaEntity entity, Request request) {
        entity.setApproverId(request.getApproverId());
        entity.setStatus(request.getStatus());
        entity.setComments(request.getComments());
        entity.setDecidedAt(request.getDecidedAt());
    }
}
