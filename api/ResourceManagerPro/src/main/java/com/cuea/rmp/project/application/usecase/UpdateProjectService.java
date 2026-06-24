package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.dto.UpdateProjectCommand;
import com.cuea.rmp.project.application.port.in.UpdateProjectUseCase;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.project.domain.Project;
import com.cuea.rmp.shared.domain.DateRange;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateProjectService implements UpdateProjectUseCase {

    private final ProjectRepository projectRepository;

    public UpdateProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResult update(UpdateProjectCommand command) {
        Project project = projectRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Project " + command.id() + " not found"));
        project.rename(command.name());
        project.updateDescription(command.description());
        project.reschedule(DateRange.of(command.startDate(), command.endDate()));
        project.changeStatus(command.status());
        return ProjectResult.from(projectRepository.save(project));
    }
}
