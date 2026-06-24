package com.cuea.rmp.project.infrastructure.persistence;

import com.cuea.rmp.project.domain.Assignment;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {

    public Assignment toDomain(AssignmentJpaEntity entity) {
        return Assignment.reconstitute(
                entity.getId(),
                entity.getProjectId(),
                entity.getResourceId(),
                entity.getTitle(),
                DateRange.of(entity.getStartDate(), entity.getEndDate()),
                entity.getAllocationPct(),
                entity.getStatus());
    }

    public AssignmentJpaEntity toNewEntity(Assignment assignment) {
        AssignmentJpaEntity entity = new AssignmentJpaEntity();
        entity.setId(assignment.getId());
        entity.setProjectId(assignment.getProjectId());
        entity.setResourceId(assignment.getResourceId());
        copyScalars(entity, assignment);
        return entity;
    }

    public void updateEntity(AssignmentJpaEntity entity, Assignment assignment) {
        copyScalars(entity, assignment);
    }

    private void copyScalars(AssignmentJpaEntity entity, Assignment assignment) {
        entity.setTitle(assignment.getTitle());
        entity.setStartDate(assignment.getPeriod().start());
        entity.setEndDate(assignment.getPeriod().end());
        entity.setAllocationPct(assignment.getAllocationPct());
        entity.setStatus(assignment.getStatus());
    }
}
