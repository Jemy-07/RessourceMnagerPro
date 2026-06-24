package com.cuea.rmp.resource.application.port.out;

import com.cuea.rmp.resource.domain.Skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound persistence port for the Skill catalog. */
public interface SkillRepository {

    Skill save(Skill skill);

    Optional<Skill> findById(UUID id);

    List<Skill> findAll();

    boolean existsByOrgIdAndName(UUID orgId, String name);
}
