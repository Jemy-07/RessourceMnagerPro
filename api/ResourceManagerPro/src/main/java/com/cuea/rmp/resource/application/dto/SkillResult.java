package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.Skill;

import java.util.UUID;

public record SkillResult(UUID id, UUID orgId, String name) {

    public static SkillResult from(Skill skill) {
        return new SkillResult(skill.getId(), skill.getOrgId(), skill.getName());
    }
}
