package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.domain.TimeOff;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Component;

@Component
public class TimeOffMapper {

    public TimeOff toDomain(TimeOffJpaEntity entity) {
        return TimeOff.reconstitute(
                entity.getId(),
                entity.getResourceId(),
                DateRange.of(entity.getStartDate(), entity.getEndDate()),
                entity.getReason(),
                entity.getStatus());
    }

    public TimeOffJpaEntity toNewEntity(TimeOff timeOff) {
        TimeOffJpaEntity entity = new TimeOffJpaEntity();
        entity.setId(timeOff.getId());
        entity.setResourceId(timeOff.getResourceId());
        entity.setStartDate(timeOff.getPeriod().start());
        entity.setEndDate(timeOff.getPeriod().end());
        entity.setReason(timeOff.getReason());
        entity.setStatus(timeOff.getStatus());
        return entity;
    }
}
