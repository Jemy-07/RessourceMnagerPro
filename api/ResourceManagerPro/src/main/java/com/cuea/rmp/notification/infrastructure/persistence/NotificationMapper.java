package com.cuea.rmp.notification.infrastructure.persistence;

import com.cuea.rmp.notification.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
                entity.getId(), entity.getUserId(), entity.getType(), entity.getMessage(), entity.isRead());
    }

    public NotificationJpaEntity toNewEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        entity.setType(notification.getType());
        entity.setMessage(notification.getMessage());
        entity.setRead(notification.isRead());
        return entity;
    }

    public void updateEntity(NotificationJpaEntity entity, Notification notification) {
        entity.setRead(notification.isRead());
    }
}
