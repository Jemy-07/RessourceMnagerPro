package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.port.in.GetProjectUseCase;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProjectService implements GetProjectUseCase {

    private final ProjectRepository projectRepository;

    public GetProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResult get(UUID id) {
        return projectRepository.findById(id)
                .map(ProjectResult::from)
                .orElseThrow(() -> new NotFoundException("Project " + id + " not found"));
    }
}
