package com.cuea.rmp.request.application.usecase;

import com.cuea.rmp.notification.application.port.in.NotificationPort;
import com.cuea.rmp.notification.domain.NotificationType;
import com.cuea.rmp.project.application.dto.AssignResourceCommand;
import com.cuea.rmp.project.application.port.in.AssignResourceUseCase;
import com.cuea.rmp.request.application.dto.ApproveRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.application.port.in.ApproveRequestUseCase;
import com.cuea.rmp.request.application.port.out.RequestRepository;
import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Approves a pending request: transitions state, creates the assignment via M5
 * (which enforces booking conflicts), and notifies the requester. If the
 * assignment conflicts, the whole transaction rolls back and the request stays
 * PENDING.
 */
@Service
@Transactional
public class ApproveRequestService implements ApproveRequestUseCase {

    private final RequestRepository requestRepository;
    private final AssignResourceUseCase assignResource;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public ApproveRequestService(RequestRepository requestRepository,
                                 AssignResourceUseCase assignResource,
                                 NotificationPort notificationPort,
                                 Clock clock) {
        this.requestRepository = requestRepository;
        this.assignResource = assignResource;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    public RequestResult approve(ApproveRequestCommand command) {
        Request request = requestRepository.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("Request " + command.requestId() + " not found"));

        request.approve(command.approverId(), Instant.now(clock));
        Request saved = requestRepository.save(request);

        // Create the assignment (throws ConflictException on over-allocation -> rollback).
        assignResource.assign(new AssignResourceCommand(
                saved.getProjectId(),
                saved.getResourceId(),
                saved.getTitle(),
                saved.getPeriod().start(),
                saved.getPeriod().end(),
                saved.getAllocationPct()));

        notificationPort.notify(saved.getRequesterId(), NotificationType.APPROVAL,
                "Your request '" + saved.getTitle() + "' was APPROVED");
        return RequestResult.from(saved);
    }
}
