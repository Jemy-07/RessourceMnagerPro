package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.dto.UpdateAssignmentCommand;
import com.cuea.rmp.project.application.port.in.UpdateAssignmentUseCase;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
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

import java.util.List;

/** Reschedules an assignment, re-running conflict detection (excluding itself). */
@Service
@Transactional
public class UpdateAssignmentService implements UpdateAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final ResourceRepository resourceRepository;
    private final TimeOffRepository timeOffRepository;
    private final ConflictDetector conflictDetector = new ConflictDetector();

    public UpdateAssignmentService(AssignmentRepository assignmentRepository,
                                   ResourceRepository resourceRepository,
                                   TimeOffRepository timeOffRepository) {
        this.assignmentRepository = assignmentRepository;
        this.resourceRepository = resourceRepository;
        this.timeOffRepository = timeOffRepository;
    }

    @Override
    public AssignmentResult update(UpdateAssignmentCommand command) {
        Assignment assignment = assignmentRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Assignment " + command.id() + " not found"));
        Resource resource = resourceRepository.findById(assignment.getResourceId())
                .orElseThrow(() -> new NotFoundException("Resource " + assignment.getResourceId() + " not found"));

        DateRange window = DateRange.of(command.startDate(), command.endDate());
        List<Assignment> others = assignmentRepository.findByResourceId(assignment.getResourceId()).stream()
                .filter(a -> !a.getId().equals(assignment.getId()))
                .toList();

        conflictDetector.detect(resource, window, command.allocationPct(), others,
                        timeOffRepository.findByResourceId(assignment.getResourceId()))
                .ifPresent(reason -> {
                    throw new ConflictException(reason, "ASSIGNMENT_CONFLICT");
                });

        assignment.reschedule(window, command.allocationPct());
        if (command.status() != null) {
            assignment.changeStatus(command.status());
        }
        return AssignmentResult.from(assignmentRepository.save(assignment));
    }
}
