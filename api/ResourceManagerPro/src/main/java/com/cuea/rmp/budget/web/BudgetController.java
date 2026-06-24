package com.cuea.rmp.budget.web;

import com.cuea.rmp.budget.application.port.in.AllocateBudgetUseCase;
import com.cuea.rmp.budget.application.port.in.GetProjectBudgetUseCase;
import com.cuea.rmp.budget.web.request.AllocateBudgetRequest;
import com.cuea.rmp.budget.web.response.BudgetResponse;
import com.cuea.rmp.shared.application.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/budget")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class BudgetController {

    private final GetProjectBudgetUseCase getBudget;
    private final AllocateBudgetUseCase allocateBudget;
    private final BudgetWebMapper mapper;

    public BudgetController(GetProjectBudgetUseCase getBudget,
                           AllocateBudgetUseCase allocateBudget,
                           BudgetWebMapper mapper) {
        this.getBudget = getBudget;
        this.allocateBudget = allocateBudget;
        this.mapper = mapper;
    }

    @GetMapping
    public ApiResponse<BudgetResponse> get(@PathVariable UUID projectId) {
        return ApiResponse.ok(mapper.toResponse(getBudget.get(projectId)));
    }

    @PutMapping
    public ApiResponse<BudgetResponse> allocate(@PathVariable UUID projectId,
                                                @Valid @RequestBody AllocateBudgetRequest request) {
        return ApiResponse.ok(
                mapper.toResponse(allocateBudget.allocate(mapper.toCommand(projectId, request))),
                "Budget allocated");
    }
}
