package com.cuea.rmp.request.application.usecase;

import com.cuea.rmp.request.application.dto.RejectRequestCommand;
import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.application.port.in.RejectRequestUseCase;
import com.cuea.rmp.request.application.port.out.NotificationPort;
import com.cuea.rmp.request.application.port.out.RequestRepository;
import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional
public class RejectRequestService implements RejectRequestUseCase {

    private final RequestRepository requestRepository;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public RejectRequestService(RequestRepository requestRepository,
                                NotificationPort notificationPort,
                                Clock clock) {
        this.requestRepository = requestRepository;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    public RequestResult reject(RejectRequestCommand command) {
        Request request = requestRepository.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("Request " + command.requestId() + " not found"));

        request.reject(command.approverId(), command.comments(), Instant.now(clock));
        Request saved = requestRepository.save(request);

        notificationPort.notify(saved.getRequesterId(),
                "Your request '" + saved.getTitle() + "' was REJECTED: " + saved.getComments());
        return RequestResult.from(saved);
    }
}
