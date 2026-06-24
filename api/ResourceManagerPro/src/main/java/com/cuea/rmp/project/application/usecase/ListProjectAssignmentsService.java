package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.port.in.ListProjectAssignmentsUseCase;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListProjectAssignmentsService implements ListProjectAssignmentsUseCase {

    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;

    public ListProjectAssignmentsService(ProjectRepository projectRepository,
                                         AssignmentRepository assignmentRepository) {
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<AssignmentResult> listByProject(UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            throw new NotFoundException("Project " + projectId + " not found");
        }
        return assignmentRepository.findByProjectId(projectId).stream()
                .map(AssignmentResult::from).toList();
    }
}
