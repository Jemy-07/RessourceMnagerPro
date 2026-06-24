package com.cuea.rmp.shared.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Inclusive date range value object. Pure Java; an end before start is rejected.
 */
public final class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    private DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public static DateRange of(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new BusinessRuleException("Range end must not be before start", "INVALID_DATE_RANGE");
        }
        return new DateRange(start, end);
    }

    /** True if this range shares at least one day with {@code other} (inclusive bounds). */
    public boolean overlaps(DateRange other) {
        return !start.isAfter(other.end) && !other.start.isAfter(end);
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof DateRange r && start.equals(r.start) && end.equals(r.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return start + ".." + end;
    }
}
