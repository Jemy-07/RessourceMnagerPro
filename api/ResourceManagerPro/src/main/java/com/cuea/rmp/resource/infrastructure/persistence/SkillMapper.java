package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.domain.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public Skill toDomain(SkillJpaEntity entity) {
        return Skill.reconstitute(entity.getId(), entity.getOrgId(), entity.getName());
    }

    public SkillJpaEntity toNewEntity(Skill skill) {
        SkillJpaEntity entity = new SkillJpaEntity();
        entity.setId(skill.getId());
        entity.setOrgId(skill.getOrgId());
        entity.setName(skill.getName());
        return entity;
    }

    public void updateEntity(SkillJpaEntity entity, Skill skill) {
        entity.setName(skill.getName());
    }
}
