package com.cuea.rmp.sync.infrastructure.engine;

import com.cuea.rmp.project.infrastructure.persistence.AssignmentJpaEntity;
import com.cuea.rmp.project.infrastructure.persistence.ProjectJpaEntity;
import com.cuea.rmp.budget.infrastructure.persistence.BudgetJpaEntity;
import com.cuea.rmp.request.infrastructure.persistence.RequestJpaEntity;
import com.cuea.rmp.resource.infrastructure.persistence.ResourceJpaEntity;
import com.cuea.rmp.sync.application.port.out.SyncableRepository;
import com.cuea.rmp.sync.domain.EntityType;
import com.cuea.rmp.timesheet.infrastructure.persistence.TimesheetJpaEntity;
import com.cuea.rmp.user.infrastructure.persistence.UserJpaEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root for the generic sync engine: builds a dedicated Jackson mapper
 * and registers one {@link SyncableRepository} per syncable aggregate. Each bean is
 * collected by {@code SyncableRepositoryRegistry}.
 */
@Configuration
public class SyncConfig {

    private final ObjectMapper syncMapper;

    public SyncConfig(ObjectMapper objectMapper) {
        this.syncMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // A resource's skills collection is not part of its scalar sync payload.
        this.syncMapper.addMixIn(ResourceJpaEntity.class, IgnoreResourceSkillsMixin.class);
    }

    @Bean
    public SyncableRepository userSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.USER, UserJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository resourceSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.RESOURCE, ResourceJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository projectSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.PROJECT, ProjectJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository assignmentSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.ASSIGNMENT, AssignmentJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository requestSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.REQUEST, RequestJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository timesheetSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.TIMESHEET, TimesheetJpaEntity.class, em, syncMapper);
    }

    @Bean
    public SyncableRepository budgetSyncableRepository(EntityManager em) {
        return new JpaSyncableRepository(EntityType.BUDGET, BudgetJpaEntity.class, em, syncMapper);
    }

    @JsonIgnoreProperties({"skills"})
    private abstract static class IgnoreResourceSkillsMixin {
    }
}
