package com.cuea.rmp.timesheet.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.ConflictException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Timesheet entry aggregate. Pure Java.
 * <p>
 * The id is supplied by the caller (offline-first: entries created on a device
 * keep their id when synced). Transitions are guarded: {@link #submit} only from
 * DRAFT, {@link #approve} only from SUBMITTED.
 */
public class Timesheet {

    private static final BigDecimal MAX_HOURS = new BigDecimal("24");

    private final UUID id;
    private final UUID resourceId;
    private final UUID assignmentId;
    private final LocalDate workDate;
    private BigDecimal hours;
    private TimesheetStatus status;

    private Timesheet(UUID id, UUID resourceId, UUID assignmentId, LocalDate workDate,
                      BigDecimal hours, TimesheetStatus status) {
        this.id = id;
        this.resourceId = resourceId;
        this.assignmentId = assignmentId;
        this.workDate = workDate;
        this.hours = hours;
        this.status = status;
    }

    /** Create a DRAFT entry with a caller-supplied id (offline-first). */
    public static Timesheet create(UUID id, UUID resourceId, UUID assignmentId,
                                   LocalDate workDate, BigDecimal hours) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(assignmentId, "assignmentId must not be null");
        Objects.requireNonNull(workDate, "workDate must not be null");
        requireHours(hours);
        return new Timesheet(id, resourceId, assignmentId, workDate, hours, TimesheetStatus.DRAFT);
    }

    public static Timesheet reconstitute(UUID id, UUID resourceId, UUID assignmentId,
                                         LocalDate workDate, BigDecimal hours, TimesheetStatus status) {
        return new Timesheet(Objects.requireNonNull(id), resourceId, assignmentId, workDate, hours, status);
    }

    public void submit() {
        if (status != TimesheetStatus.DRAFT) {
            throw new ConflictException("Only a DRAFT timesheet can be submitted (was " + status + ")",
                    "INVALID_TIMESHEET_STATE");
        }
        this.status = TimesheetStatus.SUBMITTED;
    }

    public void approve() {
        if (status != TimesheetStatus.SUBMITTED) {
            throw new ConflictException("Only a SUBMITTED timesheet can be approved (was " + status + ")",
                    "INVALID_TIMESHEET_STATE");
        }
        this.status = TimesheetStatus.APPROVED;
    }

    private static void requireHours(BigDecimal hours) {
        Objects.requireNonNull(hours, "hours must not be null");
        if (hours.signum() <= 0 || hours.compareTo(MAX_HOURS) > 0) {
            throw new BusinessRuleException("hours must be between 0 (exclusive) and 24", "INVALID_HOURS");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public TimesheetStatus getStatus() {
        return status;
    }
}
