package com.cuea.rmp.request.infrastructure.persistence;

import com.cuea.rmp.request.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestJpaRepository extends JpaRepository<RequestJpaEntity, UUID> {

    Optional<RequestJpaEntity> findByIdAndDeletedFalse(UUID id);

    List<RequestJpaEntity> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<RequestJpaEntity> findByStatusAndDeletedFalseOrderByCreatedAtDesc(RequestStatus status);
}
