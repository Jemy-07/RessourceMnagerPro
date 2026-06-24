package com.cuea.rmp.resource.domain;

import com.cuea.rmp.shared.domain.DateRange;

import java.util.List;

/**
 * Pure domain service that decides whether a resource is free across a window,
 * given its existing assignment periods and time-off. No framework dependencies.
 */
public class AvailabilityChecker {

    public AvailabilityVerdict check(Resource resource,
                                     DateRange window,
                                     List<DateRange> assignmentPeriods,
                                     List<TimeOff> timeOffs) {
        if (!resource.isAvailableStatus()) {
            return AvailabilityVerdict.blocked("Resource is marked " + resource.getAvailabilityStatus());
        }
        boolean assignmentClash = assignmentPeriods.stream().anyMatch(window::overlaps);
        if (assignmentClash) {
            return AvailabilityVerdict.blocked("Overlaps an existing assignment");
        }
        boolean timeOffClash = timeOffs.stream().anyMatch(t -> t.blocks(window));
        if (timeOffClash) {
            return AvailabilityVerdict.blocked("Overlaps approved time-off");
        }
        return AvailabilityVerdict.free();
    }
}
