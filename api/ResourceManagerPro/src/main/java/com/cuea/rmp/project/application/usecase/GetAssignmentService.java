package com.cuea.rmp.project.application.usecase;

import com.cuea.rmp.project.application.dto.AssignmentResult;
import com.cuea.rmp.project.application.port.in.GetAssignmentUseCase;
import com.cuea.rmp.project.application.port.out.AssignmentRepository;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAssignmentService implements GetAssignmentUseCase {

    private final AssignmentRepository assignmentRepository;

    public GetAssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public AssignmentResult get(UUID id) {
        return assignmentRepository.findById(id)
                .map(AssignmentResult::from)
                .orElseThrow(() -> new NotFoundException("Assignment " + id + " not found"));
    }
}
