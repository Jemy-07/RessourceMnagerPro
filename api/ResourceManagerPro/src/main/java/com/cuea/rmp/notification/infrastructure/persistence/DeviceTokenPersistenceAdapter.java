package com.cuea.rmp.notification.infrastructure.persistence;

import com.cuea.rmp.notification.application.port.out.DeviceTokenRepository;
import com.cuea.rmp.notification.domain.DeviceToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DeviceTokenPersistenceAdapter implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpaRepository;
    private final DeviceTokenMapper mapper;

    public DeviceTokenPersistenceAdapter(DeviceTokenJpaRepository jpaRepository, DeviceTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        DeviceTokenJpaEntity entity = jpaRepository.findById(deviceToken.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, deviceToken);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(deviceToken));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<DeviceToken> findByFcmToken(String fcmToken) {
        return jpaRepository.findByFcmTokenAndDeletedFalse(fcmToken).map(mapper::toDomain);
    }

    @Override
    public List<DeviceToken> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndDeletedFalse(userId).stream().map(mapper::toDomain).toList();
    }
}
