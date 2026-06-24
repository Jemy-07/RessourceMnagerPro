package com.cuea.rmp.resource.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.shared.domain.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Resource aggregate root. Owns its {@link ResourceSkill} entries. Pure Java.
 * <p>
 * A resource may optionally be linked to a platform user ({@code userId}, e.g.
 * for HUMAN resources).
 */
public class Resource {

    private final UUID id;
    private final UUID orgId;
    private UUID userId;
    private String name;
    private ResourceType type;
    private Money hourlyRate;
    private AvailabilityStatus availabilityStatus;
    private final List<ResourceSkill> skills;

    private Resource(UUID id, UUID orgId, UUID userId, String name, ResourceType type,
                     Money hourlyRate, AvailabilityStatus availabilityStatus, List<ResourceSkill> skills) {
        this.id = id;
        this.orgId = orgId;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.availabilityStatus = availabilityStatus;
        this.skills = skills;
    }

    public static Resource create(UUID orgId, UUID userId, String name, ResourceType type, Money hourlyRate) {
        if (orgId == null) {
            throw new BusinessRuleException("orgId must not be null", "INVALID_RESOURCE");
        }
        requireName(name);
        Objects.requireNonNull(type, "type must not be null");
        requireRate(hourlyRate);
        return new Resource(UUID.randomUUID(), orgId, userId, name.trim(), type, hourlyRate,
                AvailabilityStatus.AVAILABLE, new ArrayList<>());
    }

    public static Resource reconstitute(UUID id, UUID orgId, UUID userId, String name, ResourceType type,
                                        Money hourlyRate, AvailabilityStatus availabilityStatus,
                                        List<ResourceSkill> skills) {
        return new Resource(Objects.requireNonNull(id), orgId, userId, name, type, hourlyRate,
                availabilityStatus, new ArrayList<>(skills));
    }

    public void rename(String newName) {
        requireName(newName);
        this.name = newName.trim();
    }

    public void changeHourlyRate(Money newRate) {
        requireRate(newRate);
        this.hourlyRate = newRate;
    }

    public void changeAvailabilityStatus(AvailabilityStatus status) {
        this.availabilityStatus = Objects.requireNonNull(status, "status must not be null");
    }

    /** Add a skill at a proficiency. Rejects duplicates of the same skill. */
    public ResourceSkill addSkill(UUID skillId, int proficiency) {
        Objects.requireNonNull(skillId, "skillId must not be null");
        boolean alreadyPresent = skills.stream().anyMatch(s -> s.getSkillId().equals(skillId));
        if (alreadyPresent) {
            throw new ConflictException("Resource already has skill " + skillId, "SKILL_ALREADY_ASSIGNED");
        }
        ResourceSkill resourceSkill = ResourceSkill.create(this.id, skillId, proficiency);
        skills.add(resourceSkill);
        return resourceSkill;
    }

    public Optional<ResourceSkill> findSkill(UUID skillId) {
        return skills.stream().filter(s -> s.getSkillId().equals(skillId)).findFirst();
    }

    public boolean isAvailableStatus() {
        return availabilityStatus == AvailabilityStatus.AVAILABLE;
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Resource name must not be blank", "INVALID_RESOURCE");
        }
    }

    private static void requireRate(Money rate) {
        Objects.requireNonNull(rate, "hourlyRate must not be null");
        if (rate.isNegative()) {
            throw new BusinessRuleException("hourlyRate must not be negative", "INVALID_RESOURCE");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public ResourceType getType() {
        return type;
    }

    public Money getHourlyRate() {
        return hourlyRate;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public List<ResourceSkill> getSkills() {
        return Collections.unmodifiableList(skills);
    }
}
