package com.cuea.rmp.project.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.DateRange;

import java.util.Objects;
import java.util.UUID;

/** Assignment of a resource to a project over a date range at an allocation %. */
public class Assignment {

    public static final int MIN_ALLOCATION = 0;
    public static final int MAX_ALLOCATION = 100;

    private final UUID id;
    private final UUID projectId;
    private final UUID resourceId;
    private String title;
    private DateRange period;
    private int allocationPct;
    private AssignmentStatus status;

    private Assignment(UUID id, UUID projectId, UUID resourceId, String title,
                       DateRange period, int allocationPct, AssignmentStatus status) {
        this.id = id;
        this.projectId = projectId;
        this.resourceId = resourceId;
        this.title = title;
        this.period = period;
        this.allocationPct = allocationPct;
        this.status = status;
    }

    public static Assignment create(UUID projectId, UUID resourceId, String title,
                                    DateRange period, int allocationPct) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        requireTitle(title);
        Objects.requireNonNull(period, "period must not be null");
        requireAllocation(allocationPct);
        return new Assignment(UUID.randomUUID(), projectId, resourceId, title.trim(),
                period, allocationPct, AssignmentStatus.TODO);
    }

    public static Assignment reconstitute(UUID id, UUID projectId, UUID resourceId, String title,
                                          DateRange period, int allocationPct, AssignmentStatus status) {
        return new Assignment(Objects.requireNonNull(id), projectId, resourceId, title,
                period, allocationPct, status);
    }

    /** Reschedule the window and/or change the allocation. */
    public void reschedule(DateRange newPeriod, int newAllocationPct) {
        Objects.requireNonNull(newPeriod, "period must not be null");
        requireAllocation(newAllocationPct);
        this.period = newPeriod;
        this.allocationPct = newAllocationPct;
    }

    public void changeStatus(AssignmentStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "status must not be null");
    }

    /** Whether this assignment consumes capacity (DONE assignments are released). */
    public boolean consumesCapacity() {
        return status != AssignmentStatus.DONE;
    }

    private static void requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Assignment title must not be blank", "INVALID_ASSIGNMENT");
        }
    }

    private static void requireAllocation(int allocationPct) {
        if (allocationPct < MIN_ALLOCATION || allocationPct > MAX_ALLOCATION) {
            throw new BusinessRuleException(
                    "allocationPct must be between %d and %d".formatted(MIN_ALLOCATION, MAX_ALLOCATION),
                    "INVALID_ALLOCATION");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getResourceId() {
        return resourceId;
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

    public AssignmentStatus getStatus() {
        return status;
    }
}
