package com.cuea.rmp.notification.infrastructure.persistence;

import com.cuea.rmp.notification.application.port.out.NotificationRepository;
import com.cuea.rmp.notification.domain.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public NotificationPersistenceAdapter(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = jpaRepository.findById(notification.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, notification);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(notification));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toDomain).toList();
    }
}
