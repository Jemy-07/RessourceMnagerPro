package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.CreateProjectCommand;
import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.port.in.CreateProjectUseCase;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.project.domain.Project;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProjectService implements CreateProjectUseCase {

    private final ProjectRepository projectRepository;

    public CreateProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResult create(CreateProjectCommand command) {
        DateRange period = DateRange.of(command.startDate(), command.endDate());
        Project project = Project.create(
                command.orgId(), command.managerId(), command.name(), command.description(), period);
        return ProjectResult.from(projectRepository.save(project));
    }
}
