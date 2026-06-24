package com.cuea.rmp.reporting.application.port.out;

import java.util.List;

/**
 * Read-model query port. Implementations use projections / native queries and
 * MUST NOT load write aggregates.
 */
public interface ReportQueryPort {

    List<ResourceUtilizationData> utilization();

    List<SkillSupplyData> skillSupply();

    List<ProjectCostData> projectCosts();
}
