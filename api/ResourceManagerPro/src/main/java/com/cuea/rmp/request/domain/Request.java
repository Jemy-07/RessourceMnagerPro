package com.cuea.rmp.request.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.shared.domain.DateRange;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Resource-booking request aggregate with a simple state machine.
 * <p>
 * A request starts {@link RequestStatus#PENDING}. {@link #approve}/{@link #reject}
 * are only valid from PENDING; a second decision raises {@link ConflictException}.
 * It carries the desired booking window/allocation/title so an assignment can be
 * created on approval.
 */
public class Request {

    private final UUID id;
    private final UUID requesterId;
    private UUID approverId;
    private final UUID resourceId;
    private final UUID projectId;
    private final String title;
    private final DateRange period;
    private final int allocationPct;
    private RequestStatus status;
    private String comments;
    private Instant decidedAt;

    private Request(UUID id, UUID requesterId, UUID approverId, UUID resourceId, UUID projectId,
                    String title, DateRange period, int allocationPct, RequestStatus status,
                    String comments, Instant decidedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.resourceId = resourceId;
        this.projectId = projectId;
        this.title = title;
        this.period = period;
        this.allocationPct = allocationPct;
        this.status = status;
        this.comments = comments;
        this.decidedAt = decidedAt;
    }

    public static Request create(UUID requesterId, UUID resourceId, UUID projectId,
                                 String title, DateRange period, int allocationPct) {
        Objects.requireNonNull(requesterId, "requesterId must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(period, "period must not be null");
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Request title must not be blank", "INVALID_REQUEST");
        }
        if (allocationPct < 0 || allocationPct > 100) {
            throw new BusinessRuleException("allocationPct must be between 0 and 100", "INVALID_REQUEST");
        }
        return new Request(UUID.randomUUID(), requesterId, null, resourceId, projectId,
                title.trim(), period, allocationPct, RequestStatus.PENDING, null, null);
    }

    public static Request reconstitute(UUID id, UUID requesterId, UUID approverId, UUID resourceId,
                                       UUID projectId, String title, DateRange period, int allocationPct,
                                       RequestStatus status, String comments, Instant decidedAt) {
        return new Request(Objects.requireNonNull(id), requesterId, approverId, resourceId, projectId,
                title, period, allocationPct, status, comments, decidedAt);
    }

    public void approve(UUID approverId, Instant decidedAt) {
        requirePending();
        this.approverId = Objects.requireNonNull(approverId, "approverId must not be null");
        this.status = RequestStatus.APPROVED;
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt must not be null");
    }

    public void reject(UUID approverId, String comments, Instant decidedAt) {
        requirePending();
        this.approverId = Objects.requireNonNull(approverId, "approverId must not be null");
        this.comments = comments;
        this.status = RequestStatus.REJECTED;
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt must not be null");
    }

    private void requirePending() {
        if (status != RequestStatus.PENDING) {
            throw new ConflictException("Request is already " + status, "REQUEST_ALREADY_DECIDED");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public UUID getApproverId() {
        return approverId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public DateRange getPeriod() {
        return period;
    }

    public int getAllocationPct() {
        return allocationPct;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getComments() {
        return comments;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
