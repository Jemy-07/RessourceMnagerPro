package com.cuea.rmp.timesheet.infrastructure.persistence;

import com.cuea.rmp.timesheet.domain.Timesheet;
import org.springframework.stereotype.Component;

@Component
public class TimesheetMapper {

    public Timesheet toDomain(TimesheetJpaEntity entity) {
        return Timesheet.reconstitute(
                entity.getId(),
                entity.getResourceId(),
                entity.getAssignmentId(),
                entity.getWorkDate(),
                entity.getHours(),
                entity.getStatus());
    }

    public TimesheetJpaEntity toNewEntity(Timesheet timesheet) {
        TimesheetJpaEntity entity = new TimesheetJpaEntity();
        entity.setId(timesheet.getId());
        entity.setResourceId(timesheet.getResourceId());
        entity.setAssignmentId(timesheet.getAssignmentId());
        entity.setWorkDate(timesheet.getWorkDate());
        entity.setHours(timesheet.getHours());
        entity.setStatus(timesheet.getStatus());
        return entity;
    }

    /** Only the mutable state (hours, status) can change after creation. */
    public void updateEntity(TimesheetJpaEntity entity, Timesheet timesheet) {
        entity.setHours(timesheet.getHours());
        entity.setStatus(timesheet.getStatus());
    }
}
