package com.cuea.rmp.request.infrastructure.persistence;

import com.cuea.rmp.request.application.port.out.RequestRepository;
import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.request.domain.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestPersistenceAdapter implements RequestRepository {

    private final RequestJpaRepository jpaRepository;
    private final RequestMapper mapper;

    public RequestPersistenceAdapter(RequestJpaRepository jpaRepository, RequestMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Request save(Request request) {
        RequestJpaEntity entity = jpaRepository.findById(request.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, request);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(request));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Request> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public List<Request> findAll() {
        return jpaRepository.findAllByDeletedFalseOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Request> findByStatus(RequestStatus status) {
        return jpaRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(status).stream()
                .map(mapper::toDomain).toList();
    }
}
