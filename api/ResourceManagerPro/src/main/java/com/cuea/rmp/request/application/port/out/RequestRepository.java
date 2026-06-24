package com.cuea.rmp.request.application.port.out;

import com.cuea.rmp.request.domain.Request;
import com.cuea.rmp.request.domain.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {

    Request save(Request request);

    Optional<Request> findById(UUID id);

    List<Request> findAll();

    List<Request> findByStatus(RequestStatus status);
}
