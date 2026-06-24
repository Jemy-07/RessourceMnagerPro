package com.cuea.rmp.request.application.usecase;

import com.cuea.rmp.request.application.dto.CreateRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.application.port.in.CreateRequestUseCase;
import com.cuea.rmp.request.application.port.out.RequestRepository;
import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.resource.application.dto.AvailabilityResult;
import com.cuea.rmp.resource.application.port.in.CheckAvailabilityUseCase;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.shared.domain.DateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a booking request. Runs the M4 availability check up-front so a request
 * is never raised for a resource that is unavailable (status / approved time-off).
 */
@Service
@Transactional
public class CreateRequestService implements CreateRequestUseCase {

    private final RequestRepository requestRepository;
    private final CheckAvailabilityUseCase checkAvailability;

    public CreateRequestService(RequestRepository requestRepository, CheckAvailabilityUseCase checkAvailability) {
        this.requestRepository = requestRepository;
        this.checkAvailability = checkAvailability;
    }

    @Override
    public RequestResult create(CreateRequestCommand command) {
        // Up-front availability check (also validates the resource exists -> 404).
        AvailabilityResult availability =
                checkAvailability.check(command.resourceId(), command.startDate(), command.endDate());
        if (!availability.available()) {
            throw new ConflictException(
                    "Resource is not available for the requested window: " + availability.reason(),
                    "RESOURCE_UNAVAILABLE");
        }

        Request request = Request.create(
                command.requesterId(),
                command.resourceId(),
                command.projectId(),
                command.title(),
                DateRange.of(command.startDate(), command.endDate()),
                command.allocationPct());
        return RequestResult.from(requestRepository.save(request));
    }
}
