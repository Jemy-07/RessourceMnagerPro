package com.cuea.rmp.project.domain;

import com.cuea.rmp.resource.domain.AvailabilityChecker;
import com.cuea.rmp.resource.domain.AvailabilityVerdict;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.resource.domain.TimeOff;
import com.cuea.rmp.shared.domain.DateRange;

import java.util.List;
import java.util.Optional;

/**
 * Domain service that prevents booking conflicts. Reuses M4's
 * {@link AvailabilityChecker} for status/time-off, then enforces capacity:
 * the sum of overlapping (non-DONE) allocations plus the requested allocation
 * must not exceed 100% — which also rules out double-booking.
 */
public class ConflictDetector {

    private final AvailabilityChecker availabilityChecker = new AvailabilityChecker();

    /**
     * @param existingAssignments other assignments for the same resource (exclude the one being updated)
     * @return a conflict reason, or empty if the booking is allowed
     */
    public Optional<String> detect(Resource resource,
                                   DateRange window,
                                   int requestedAllocationPct,
                                   List<Assignment> existingAssignments,
                                   List<TimeOff> timeOffs) {
        // 1) resource status + approved time-off (assignment overlaps handled below)
        AvailabilityVerdict verdict = availabilityChecker.check(resource, window, List.of(), timeOffs);
        if (!verdict.available()) {
            return Optional.of(verdict.reason());
        }

        // 2) capacity: overlapping allocations must not exceed 100%
        int overlappingAllocation = existingAssignments.stream()
                .filter(Assignment::consumesCapacity)
                .filter(a -> a.getPeriod().overlaps(window))
                .mapToInt(Assignment::getAllocationPct)
                .sum();

        if (overlappingAllocation + requestedAllocationPct > Assignment.MAX_ALLOCATION) {
            return Optional.of(
                    "Over-allocation: %d%% already booked + %d%% requested exceeds 100%% for the window"
                            .formatted(overlappingAllocation, requestedAllocationPct));
        }
        return Optional.empty();
    }
}
