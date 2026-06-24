package com.cuea.rmp.resource.domain;

/** Outcome of an availability check: whether free, and if not, why. */
public record AvailabilityVerdict(boolean available, String reason) {

    public static AvailabilityVerdict free() {
        return new AvailabilityVerdict(true, "Available");
    }

    public static AvailabilityVerdict blocked(String reason) {
        return new AvailabilityVerdict(false, reason);
    }
}
