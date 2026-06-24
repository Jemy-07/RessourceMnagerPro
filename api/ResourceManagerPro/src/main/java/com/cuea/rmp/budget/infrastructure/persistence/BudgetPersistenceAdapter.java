package com.cuea.rmp.budget.infrastructure.persistence;

import com.cuea.rmp.budget.application.port.out.BudgetRepository;
import com.cuea.rmp.budget.domain.Budget;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetPersistenceAdapter implements BudgetRepository {

    private final BudgetJpaRepository jpaRepository;
    private final BudgetMapper mapper;

    public BudgetPersistenceAdapter(BudgetJpaRepository jpaRepository, BudgetMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Budget save(Budget budget) {
        BudgetJpaEntity entity = jpaRepository.findById(budget.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, budget);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(budget));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Budget> findByProjectId(UUID projectId) {
        return jpaRepository.findByProjectIdAndDeletedFalse(projectId).map(mapper::toDomain);
    }
}
