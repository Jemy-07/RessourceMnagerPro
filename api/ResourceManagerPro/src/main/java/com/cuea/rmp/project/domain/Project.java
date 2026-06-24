package com.cuea.rmp.project.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.DateRange;

import java.util.Objects;
import java.util.UUID;

/** Project aggregate root. Pure Java. */
public class Project {

    private final UUID id;
    private final UUID orgId;
    private UUID managerId;
    private String name;
    private String description;
    private DateRange period;
    private ProjectStatus status;

    private Project(UUID id, UUID orgId, UUID managerId, String name, String description,
                    DateRange period, ProjectStatus status) {
        this.id = id;
        this.orgId = orgId;
        this.managerId = managerId;
        this.name = name;
        this.description = description;
        this.period = period;
        this.status = status;
    }

    public static Project create(UUID orgId, UUID managerId, String name, String description, DateRange period) {
        if (orgId == null) {
            throw new BusinessRuleException("orgId must not be null", "INVALID_PROJECT");
        }
        if (managerId == null) {
            throw new BusinessRuleException("managerId must not be null", "INVALID_PROJECT");
        }
        requireName(name);
        Objects.requireNonNull(period, "period must not be null");
        return new Project(UUID.randomUUID(), orgId, managerId, name.trim(), description, period, ProjectStatus.PLANNED);
    }

    public static Project reconstitute(UUID id, UUID orgId, UUID managerId, String name, String description,
                                       DateRange period, ProjectStatus status) {
        return new Project(Objects.requireNonNull(id), orgId, managerId, name, description, period, status);
    }

    public void rename(String newName) {
        requireName(newName);
        this.name = newName.trim();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public void reschedule(DateRange newPeriod) {
        this.period = Objects.requireNonNull(newPeriod, "period must not be null");
    }

    public void changeStatus(ProjectStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "status must not be null");
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Project name must not be blank", "INVALID_PROJECT");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DateRange getPeriod() {
        return period;
    }

    public ProjectStatus getStatus() {
        return status;
    }
}
