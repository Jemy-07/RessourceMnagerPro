package com.cuea.rmp.project.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, UUID> {

    Optional<ProjectJpaEntity> findByIdAndDeletedFalse(UUID id);

    Page<ProjectJpaEntity> findAllByDeletedFalse(Pageable pageable);
}
