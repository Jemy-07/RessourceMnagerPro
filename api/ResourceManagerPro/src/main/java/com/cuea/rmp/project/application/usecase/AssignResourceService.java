package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.AssignResourceCommand;
import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.port.in.AssignResourceUseCase;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.project.application.port.out.ProjectRepository;
import com.cuea.rmp.project.domain.Assignment;
import com.cuea.rmp.project.domain.ConflictDetector;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.application.port.out.TimeOffRepository;
import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.shared.domain.DateRange;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignResourceService implements AssignResourceUseCase {

    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;
    private final TimeOffRepository timeOffRepository;
    private final ConflictDetector conflictDetector = new ConflictDetector();

    public AssignResourceService(ProjectRepository projectRepository,
                                 AssignmentRepository assignmentRepository,
                                 ResourceRepository resourceRepository,
                                 TimeOffRepository timeOffRepository) {
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.resourceRepository = resourceRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Override
    public AssignmentResult assign(AssignResourceCommand command) {
        if (projectRepository.findById(command.projectId()).isEmpty()) {
            throw new NotFoundException("Project " + command.projectId() + " not found");
        }
        Resource resource = resourceRepository.findById(command.resourceId())
                .orElseThrow(() -> new NotFoundException("Resource " + command.resourceId() + " not found"));

        DateRange window = DateRange.of(command.startDate(), command.endDate());
        conflictDetector.detect(
                        resource,
                        window,
                        command.allocationPct(),
                        assignmentRepository.findByResourceId(command.resourceId()),
                        timeOffRepository.findByResourceId(command.resourceId()))
                .ifPresent(reason -> {
                    throw new ConflictException(reason, "ASSIGNMENT_CONFLICT");
                });

        Assignment assignment = Assignment.create(
                command.projectId(), command.resourceId(), command.title(), window, command.allocationPct());
        return AssignmentResult.from(assignmentRepository.save(assignment));
    }
}
