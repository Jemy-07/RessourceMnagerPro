package com.cuea.rmp.budget.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, UUID> {

    Optional<BudgetJpaEntity> findByProjectIdAndDeletedFalse(UUID projectId);
}
