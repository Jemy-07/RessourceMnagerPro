package com.cuea.rmp.project.infrastructure.persistence;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.project.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ProjectPersistenceAdapter implements ProjectRepository {

    private final ProjectJpaRepository jpaRepository;
    private final ProjectMapper mapper;

    public ProjectPersistenceAdapter(ProjectJpaRepository jpaRepository, ProjectMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Project save(Project project) {
        ProjectJpaEntity entity = jpaRepository.findById(project.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, project);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(project));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Project> findAll(int page, int size) {
        Page<ProjectJpaEntity> result = jpaRepository.findAllByDeletedFalse(PageRequest.of(page, size));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public void softDelete(UUID id) {
        jpaRepository.findByIdAndDeletedFalse(id).ifPresent(entity -> {
            entity.setDeleted(true);
            jpaRepository.save(entity);
        });
    }
}
