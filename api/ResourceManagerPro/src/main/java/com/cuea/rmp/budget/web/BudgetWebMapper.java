package com.cuea.rmp.budget.web;

import com.cuea.rmp.budget.application.dto.AllocateBudgetCommand;
import com.cuea.rmp.budget.application.dto.BudgetResult;
import com.cuea.rmp.budget.web.request.AllocateBudgetRequest;
import com.cuea.rmp.budget.web.response.BudgetResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BudgetWebMapper {

    public AllocateBudgetCommand toCommand(UUID projectId, AllocateBudgetRequest request) {
        return new AllocateBudgetCommand(
                projectId, request.totalAmount(), request.allocatedAmount(), request.currency());
    }

    public BudgetResponse toResponse(BudgetResult result) {
        return new BudgetResponse(
                result.id(),
                result.projectId(),
                result.currency(),
                result.totalAmount(),
                result.allocatedAmount(),
                result.spentAmount(),
                result.margin(),
                result.remaining());
    }
}
