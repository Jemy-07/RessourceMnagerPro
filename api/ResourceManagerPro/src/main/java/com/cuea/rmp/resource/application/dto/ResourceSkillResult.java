package com.cuea.rmp.resource.application.dto;

import com.cuea.rmp.resource.domain.ResourceSkill;

import java.util.UUID;

public record ResourceSkillResult(UUID skillId, int proficiency) {

    public static ResourceSkillResult from(ResourceSkill skill) {
        return new ResourceSkillResult(skill.getSkillId(), skill.getProficiency());
    }
}
