package com.cuea.rmp.request.application.usecase;

import com.cuea.rmp.request.application.dto.RequestResult;
import com.cuea.rmp.request.application.port.in.ListRequestsUseCase;
import com.cuea.rmp.request.application.port.out.RequestRepository;
import com.cuea.rmp.request.domain.RequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListRequestsService implements ListRequestsUseCase {

    private final RequestRepository requestRepository;

    public ListRequestsService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public List<RequestResult> list(RequestStatus status) {
        List<com.cuea.rmp.request.domain.Request> requests = (status == null)
                ? requestRepository.findAll()
                : requestRepository.findByStatus(status);
        return requests.stream().map(RequestResult::from).toList();
    }
}
