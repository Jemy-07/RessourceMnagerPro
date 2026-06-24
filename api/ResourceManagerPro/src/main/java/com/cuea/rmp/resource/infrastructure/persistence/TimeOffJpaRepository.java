package com.cuea.rmp.resource.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeOffJpaRepository extends JpaRepository<TimeOffJpaEntity, UUID> {

    List<TimeOffJpaEntity> findByResourceIdAndDeletedFalse(UUID resourceId);
}
