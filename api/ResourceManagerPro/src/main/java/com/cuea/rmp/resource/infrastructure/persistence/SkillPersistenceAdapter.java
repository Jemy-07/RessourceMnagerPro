package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.application.port.out.SkillRepository;
import com.cuea.rmp.resource.domain.Skill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SkillPersistenceAdapter implements SkillRepository {

    private final SkillJpaRepository jpaRepository;
    private final SkillMapper mapper;

    public SkillPersistenceAdapter(SkillJpaRepository jpaRepository, SkillMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Skill save(Skill skill) {
        SkillJpaEntity entity = jpaRepository.findById(skill.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, skill);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(skill));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public List<Skill> findAll() {
        return jpaRepository.findAllByDeletedFalse().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByOrgIdAndName(UUID orgId, String name) {
        return jpaRepository.existsByOrgIdAndNameAndDeletedFalse(orgId, name);
    }
}
