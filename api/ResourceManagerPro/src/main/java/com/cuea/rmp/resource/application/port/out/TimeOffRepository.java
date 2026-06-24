package com.cuea.rmp.resource.application.port.out;

import com.cuea.rmp.resource.domain.TimeOff;

import java.util.List;
import java.util.UUID;

/** Outbound persistence port for resource time-off. */
public interface TimeOffRepository {

    TimeOff save(TimeOff timeOff);

    List<TimeOff> findByResourceId(UUID resourceId);
}
