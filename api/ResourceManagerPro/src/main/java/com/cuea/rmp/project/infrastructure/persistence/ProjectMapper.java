package com.cuea.rmp.project.infrastructure.persistence;

import com.cuea.rmp.project.domain.Project;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toDomain(ProjectJpaEntity entity) {
        return Project.reconstitute(
                entity.getId(),
                entity.getOrgId(),
                entity.getManagerId(),
                entity.getName(),
                entity.getDescription(),
                DateRange.of(entity.getStartDate(), entity.getEndDate()),
                entity.getStatus());
    }

    public ProjectJpaEntity toNewEntity(Project project) {
        ProjectJpaEntity entity = new ProjectJpaEntity();
        entity.setId(project.getId());
        entity.setOrgId(project.getOrgId());
        copyScalars(entity, project);
        return entity;
    }

    public void updateEntity(ProjectJpaEntity entity, Project project) {
        copyScalars(entity, project);
    }

    private void copyScalars(ProjectJpaEntity entity, Project project) {
        entity.setManagerId(project.getManagerId());
        entity.setName(project.getName());
        entity.setDescription(project.getDescription());
        entity.setStartDate(project.getPeriod().start());
        entity.setEndDate(project.getPeriod().end());
        entity.setStatus(project.getStatus());
    }
}
