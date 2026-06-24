package com.cuea.rmp.resource.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.UUID;

/** Skill catalog entry, scoped to an organisation. */
public class Skill {

    private final UUID id;
    private final UUID orgId;
    private String name;

    private Skill(UUID id, UUID orgId, String name) {
        this.id = id;
        this.orgId = orgId;
        this.name = name;
    }

    public static Skill create(UUID orgId, String name) {
        if (orgId == null) {
            throw new BusinessRuleException("orgId must not be null", "INVALID_SKILL");
        }
        requireName(name);
        return new Skill(UUID.randomUUID(), orgId, name.trim());
    }

    public static Skill reconstitute(UUID id, UUID orgId, String name) {
        return new Skill(Objects.requireNonNull(id), orgId, name);
    }

    public void rename(String newName) {
        requireName(newName);
        this.name = newName.trim();
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Skill name must not be blank", "INVALID_SKILL");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getName() {
        return name;
    }
}
