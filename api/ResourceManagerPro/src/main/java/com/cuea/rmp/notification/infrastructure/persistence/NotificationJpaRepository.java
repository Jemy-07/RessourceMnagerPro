package com.cuea.rmp.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    Optional<NotificationJpaEntity> findByIdAndDeletedFalse(UUID id);

    List<NotificationJpaEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId);
}
