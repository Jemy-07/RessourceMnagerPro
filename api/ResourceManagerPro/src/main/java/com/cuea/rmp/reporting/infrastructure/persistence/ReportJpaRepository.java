package com.cuea.rmp.reporting.infrastructure.persistence;

import com.cuea.rmp.resource.infrastructure.persistence.ResourceJpaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Read-only reporting queries (native, projection-based). Bound to an arbitrary
 * managed entity only to satisfy Spring Data; the queries are native and never
 * load write aggregates.
 */
public interface ReportJpaRepository extends Repository<ResourceJpaEntity, UUID> {

    @Query(value = """
            SELECT CAST(r.id AS CHAR) AS resourceId,
                   r.name             AS resourceName,
                   CAST(COUNT(a.id) AS SIGNED)                          AS activeAssignments,
                   CAST(COALESCE(SUM(a.allocation_pct), 0) AS SIGNED)   AS allocatedPct
            FROM resources r
            LEFT JOIN assignments a
                   ON a.resource_id = r.id AND a.deleted = FALSE AND a.status <> 'DONE'
            WHERE r.deleted = FALSE
            GROUP BY r.id, r.name
            ORDER BY allocatedPct DESC, r.name ASC
            """, nativeQuery = true)
    List<UtilizationProjection> utilization();

    @Query(value = """
            SELECT CAST(s.id AS CHAR) AS skillId,
                   s.name             AS skillName,
                   CAST(COUNT(rs.id) AS SIGNED)                       AS resourceCount,
                   CAST(COALESCE(AVG(rs.proficiency), 0) AS DOUBLE)   AS avgProficiency
            FROM skills s
            LEFT JOIN resource_skills rs
                   ON rs.skill_id = s.id AND rs.deleted = FALSE
            WHERE s.deleted = FALSE
            GROUP BY s.id, s.name
            ORDER BY resourceCount ASC, s.name ASC
            """, nativeQuery = true)
    List<SkillSupplyProjection> skillSupply();

    @Query(value = """
            SELECT CAST(p.id AS CHAR) AS projectId,
                   p.name             AS projectName,
                   b.currency         AS currency,
                   b.total_amount     AS totalAmount,
                   b.allocated_amount AS allocatedAmount,
                   b.spent_amount     AS spentAmount
            FROM projects p
            JOIN budgets b ON b.project_id = p.id AND b.deleted = FALSE
            WHERE p.deleted = FALSE
            ORDER BY p.name ASC
            """, nativeQuery = true)
    List<ProjectCostProjection> projectCosts();
}
