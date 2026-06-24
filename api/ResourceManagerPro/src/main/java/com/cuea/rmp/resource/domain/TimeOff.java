package com.cuea.rmp.resource.domain;

import com.cuea.rmp.shared.domain.DateRange;

import java.util.Objects;
import java.util.UUID;

/** A period during which a resource is unavailable. */
public class TimeOff {

    private final UUID id;
    private final UUID resourceId;
    private final DateRange period;
    private final String reason;
    private TimeOffStatus status;

    private TimeOff(UUID id, UUID resourceId, DateRange period, String reason, TimeOffStatus status) {
        this.id = id;
        this.resourceId = resourceId;
        this.period = period;
        this.reason = reason;
        this.status = status;
    }

    public static TimeOff create(UUID resourceId, DateRange period, String reason) {
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(period, "period must not be null");
        return new TimeOff(UUID.randomUUID(), resourceId, period, reason, TimeOffStatus.PENDING);
    }

    public static TimeOff reconstitute(UUID id, UUID resourceId, DateRange period, String reason, TimeOffStatus status) {
        return new TimeOff(Objects.requireNonNull(id), resourceId, period, reason, status);
    }

    public void approve() {
        this.status = TimeOffStatus.APPROVED;
    }

    public void reject() {
        this.status = TimeOffStatus.REJECTED;
    }

    public boolean blocks(DateRange window) {
        return status == TimeOffStatus.APPROVED && period.overlaps(window);
    }

    public UUID getId() {
        return id;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public DateRange getPeriod() {
        return period;
    }

    public String getReason() {
        return reason;
    }

    public TimeOffStatus getStatus() {
        return status;
    }
}
