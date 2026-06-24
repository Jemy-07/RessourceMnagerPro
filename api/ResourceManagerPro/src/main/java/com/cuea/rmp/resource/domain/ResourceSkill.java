package com.cuea.rmp.resource.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.UUID;

/**
 * A skill held by a resource at a given proficiency (1–5). Part of the Resource
 * aggregate.
 */
public class ResourceSkill {

    public static final int MIN_PROFICIENCY = 1;
    public static final int MAX_PROFICIENCY = 5;

    private final UUID id;
    private final UUID resourceId;
    private final UUID skillId;
    private int proficiency;

    private ResourceSkill(UUID id, UUID resourceId, UUID skillId, int proficiency) {
        this.id = id;
        this.resourceId = resourceId;
        this.skillId = skillId;
        this.proficiency = proficiency;
    }

    public static ResourceSkill create(UUID resourceId, UUID skillId, int proficiency) {
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        Objects.requireNonNull(skillId, "skillId must not be null");
        requireProficiency(proficiency);
        return new ResourceSkill(UUID.randomUUID(), resourceId, skillId, proficiency);
    }

    public static ResourceSkill reconstitute(UUID id, UUID resourceId, UUID skillId, int proficiency) {
        return new ResourceSkill(Objects.requireNonNull(id), resourceId, skillId, proficiency);
    }

    public void changeProficiency(int proficiency) {
        requireProficiency(proficiency);
        this.proficiency = proficiency;
    }

    private static void requireProficiency(int proficiency) {
        if (proficiency < MIN_PROFICIENCY || proficiency > MAX_PROFICIENCY) {
            throw new BusinessRuleException(
                    "proficiency must be between %d and %d".formatted(MIN_PROFICIENCY, MAX_PROFICIENCY),
                    "INVALID_PROFICIENCY");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public int getProficiency() {
        return proficiency;
    }
}
