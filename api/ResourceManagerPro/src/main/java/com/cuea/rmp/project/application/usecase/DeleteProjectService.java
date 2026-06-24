package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.port.in.DeleteProjectUseCase;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteProjectService implements DeleteProjectUseCase {

    private final ProjectRepository projectRepository;

    public DeleteProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void delete(UUID id) {
        if (projectRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Project " + id + " not found");
        }
        projectRepository.softDelete(id);
    }
}
