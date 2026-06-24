package com.cuea.rmp.notification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpaEntity, UUID> {

    Optional<DeviceTokenJpaEntity> findByFcmTokenAndDeletedFalse(String fcmToken);

    List<DeviceTokenJpaEntity> findByUserIdAndDeletedFalse(UUID userId);
}
