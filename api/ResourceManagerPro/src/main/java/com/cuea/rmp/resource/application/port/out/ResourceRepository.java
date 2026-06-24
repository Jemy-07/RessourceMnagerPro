package com.cuea.rmp.resource.application.port.out;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.domain.Resource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound persistence port for the Resource aggregate (incl. its skills). */
public interface ResourceRepository {

    Resource save(Resource resource);

    Optional<Resource> findById(UUID id);

    PageResult<Resource> findAll(int page, int size);

    /** Resources (non-deleted) that hold the given skill, with skills loaded. */
    List<Resource> findBySkill(UUID skillId);

    void softDelete(UUID id);
}
