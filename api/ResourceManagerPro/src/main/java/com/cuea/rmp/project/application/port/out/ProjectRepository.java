package com.cuea.rmp.project.application.port.out;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.domain.Project;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    PageResult<Project> findAll(int page, int size);

    void softDelete(UUID id);
}
