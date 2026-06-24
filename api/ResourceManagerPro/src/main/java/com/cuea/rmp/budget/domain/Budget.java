package com.cuea.rmp.budget.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Budget aggregate, one-to-one with a project. Tracks total, allocated, and spent
 * amounts in a single currency. Pure Java.
 * <ul>
 *   <li>{@code margin()}    = total − spent</li>
 *   <li>{@code remaining()} = total − allocated</li>
 * </ul>
 */
public class Budget {

    private final UUID id;
    private final UUID projectId;
    private Money totalAmount;
    private Money allocatedAmount;
    private Money spentAmount;

    private Budget(UUID id, UUID projectId, Money totalAmount, Money allocatedAmount, Money spentAmount) {
        this.id = id;
        this.projectId = projectId;
        this.totalAmount = totalAmount;
        this.allocatedAmount = allocatedAmount;
        this.spentAmount = spentAmount;
    }

    /** Create a fresh budget with the given total; allocated and spent start at zero. */
    public static Budget create(UUID projectId, Money totalAmount) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Money zero = Money.of(BigDecimal.ZERO, totalAmount.getCurrency());
        Budget budget = new Budget(UUID.randomUUID(), projectId, totalAmount, zero, zero);
        budget.requireNonNegative(totalAmount, "totalAmount");
        return budget;
    }

    public static Budget reconstitute(UUID id, UUID projectId, Money totalAmount,
                                      Money allocatedAmount, Money spentAmount) {
        return new Budget(Objects.requireNonNull(id), projectId, totalAmount, allocatedAmount, spentAmount);
    }

    /** Set the total and allocated amounts (allocation must not exceed total). */
    public void allocate(Money totalAmount, Money allocatedAmount) {
        Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        Objects.requireNonNull(allocatedAmount, "allocatedAmount must not be null");
        requireSameCurrency(totalAmount);
        requireSameCurrency(allocatedAmount);
        requireNonNegative(totalAmount, "totalAmount");
        requireNonNegative(allocatedAmount, "allocatedAmount");
        if (allocatedAmount.subtract(totalAmount).getAmount().signum() > 0) {
            throw new BusinessRuleException("allocatedAmount must not exceed totalAmount", "BUDGET_OVER_ALLOCATED");
        }
        this.totalAmount = totalAmount;
        this.allocatedAmount = allocatedAmount;
    }

    public void recalculateSpend(Money spentAmount) {
        Objects.requireNonNull(spentAmount, "spentAmount must not be null");
        requireSameCurrency(spentAmount);
        requireNonNegative(spentAmount, "spentAmount");
        this.spentAmount = spentAmount;
    }

    /** total − spent */
    public Money margin() {
        return totalAmount.subtract(spentAmount);
    }

    /** total − allocated */
    public Money remaining() {
        return totalAmount.subtract(allocatedAmount);
    }

    public String getCurrency() {
        return totalAmount.getCurrency();
    }

    private void requireSameCurrency(Money money) {
        if (!money.getCurrency().equals(getCurrency())) {
            throw new BusinessRuleException(
                    "Currency mismatch: budget is " + getCurrency() + " but got " + money.getCurrency(),
                    "CURRENCY_MISMATCH");
        }
    }

    private void requireNonNegative(Money money, String field) {
        if (money.isNegative()) {
            throw new BusinessRuleException(field + " must not be negative", "INVALID_BUDGET");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Money getAllocatedAmount() {
        return allocatedAmount;
    }

    public Money getSpentAmount() {
        return spentAmount;
    }
}
