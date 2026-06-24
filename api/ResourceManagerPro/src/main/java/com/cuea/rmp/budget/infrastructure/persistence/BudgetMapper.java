package com.cuea.rmp.budget.infrastructure.persistence;

import com.cuea.rmp.budget.domain.Budget;
import com.cuea.rmp.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public Budget toDomain(BudgetJpaEntity entity) {
        String currency = entity.getCurrency();
        return Budget.reconstitute(
                entity.getId(),
                entity.getProjectId(),
                Money.of(entity.getTotalAmount(), currency),
                Money.of(entity.getAllocatedAmount(), currency),
                Money.of(entity.getSpentAmount(), currency));
    }

    public BudgetJpaEntity toNewEntity(Budget budget) {
        BudgetJpaEntity entity = new BudgetJpaEntity();
        entity.setId(budget.getId());
        entity.setProjectId(budget.getProjectId());
        copyAmounts(entity, budget);
        return entity;
    }

    public void updateEntity(BudgetJpaEntity entity, Budget budget) {
        copyAmounts(entity, budget);
    }

    private void copyAmounts(BudgetJpaEntity entity, Budget budget) {
        entity.setCurrency(budget.getCurrency());
        entity.setTotalAmount(budget.getTotalAmount().getAmount());
        entity.setAllocatedAmount(budget.getAllocatedAmount().getAmount());
        entity.setSpentAmount(budget.getSpentAmount().getAmount());
    }
}
