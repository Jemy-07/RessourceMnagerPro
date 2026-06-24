package com.cuea.rmp.project.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentJpaRepository extends JpaRepository<AssignmentJpaEntity, UUID> {

    Optional<AssignmentJpaEntity> findByIdAndDeletedFalse(UUID id);

    List<AssignmentJpaEntity> findByProjectIdAndDeletedFalse(UUID projectId);

    List<AssignmentJpaEntity> findByResourceIdAndDeletedFalse(UUID resourceId);
}
