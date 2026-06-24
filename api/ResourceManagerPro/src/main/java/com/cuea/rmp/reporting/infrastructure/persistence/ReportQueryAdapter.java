package com.cuea.rmp.reporting.infrastructure.persistence;

import com.cuea.rmp.reporting.application.port.out.ProjectCostData;
import com.cuea.rmp.reporting.application.port.out.ReportQueryPort;
import com.cuea.rmp.reporting.application.port.out.ResourceUtilizationData;
import com.cuea.rmp.reporting.application.port.out.SkillSupplyData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReportQueryAdapter implements ReportQueryPort {

    private final ReportJpaRepository repository;

    public ReportQueryAdapter(ReportJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ResourceUtilizationData> utilization() {
        return repository.utilization().stream()
                .map(p -> new ResourceUtilizationData(
                        UUID.fromString(p.getResourceId()), p.getResourceName(),
                        p.getActiveAssignments(), p.getAllocatedPct()))
                .toList();
    }

    @Override
    public List<SkillSupplyData> skillSupply() {
        return repository.skillSupply().stream()
                .map(p -> new SkillSupplyData(
                        UUID.fromString(p.getSkillId()), p.getSkillName(),
                        p.getResourceCount(), p.getAvgProficiency()))
                .toList();
    }

    @Override
    public List<ProjectCostData> projectCosts() {
        return repository.projectCosts().stream()
                .map(p -> new ProjectCostData(
                        UUID.fromString(p.getProjectId()), p.getProjectName(), p.getCurrency(),
                        p.getTotalAmount(), p.getAllocatedAmount(), p.getSpentAmount()))
                .toList();
    }
}
