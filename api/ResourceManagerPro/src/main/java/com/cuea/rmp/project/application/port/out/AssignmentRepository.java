package com.cuea.rmp.project.application.port.out;

import com.cuea.rmp.project.domain.Assignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository {

    Assignment save(Assignment assignment);

    Optional<Assignment> findById(UUID id);

    List<Assignment> findByProjectId(UUID projectId);

    List<Assignment> findByResourceId(UUID resourceId);
}
