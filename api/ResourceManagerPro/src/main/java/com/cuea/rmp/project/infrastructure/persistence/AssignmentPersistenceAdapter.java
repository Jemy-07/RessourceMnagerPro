package com.cuea.rmp.project.infrastructure.persistence;

import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.project.domain.Assignment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssignmentPersistenceAdapter implements AssignmentRepository {

    private final AssignmentJpaRepository jpaRepository;
    private final AssignmentMapper mapper;

    public AssignmentPersistenceAdapter(AssignmentJpaRepository jpaRepository, AssignmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Assignment save(Assignment assignment) {
        AssignmentJpaEntity entity = jpaRepository.findById(assignment.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, assignment);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(assignment));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Assignment> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public List<Assignment> findByProjectId(UUID projectId) {
        return jpaRepository.findByProjectIdAndDeletedFalse(projectId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Assignment> findByResourceId(UUID resourceId) {
        return jpaRepository.findByResourceIdAndDeletedFalse(resourceId).stream().map(mapper::toDomain).toList();
    }
}
