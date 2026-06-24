package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.project.application.dto.ProjectResult;
import com.cuea.rmp.project.application.port.in.ListProjectsUseCase;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.project.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListProjectsService implements ListProjectsUseCase {

    private final ProjectRepository projectRepository;

    public ListProjectsService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public PageResult<ProjectResult> list(int page, int size) {
        PageResult<Project> projects = projectRepository.findAll(page, size);
        return new PageResult<>(
                projects.content().stream().map(ProjectResult::from).toList(),
                projects.page(),
                projects.size(),
                projects.totalElements(),
                projects.totalPages());
    }
}
