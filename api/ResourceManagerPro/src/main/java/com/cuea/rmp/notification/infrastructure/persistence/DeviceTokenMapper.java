package com.cuea.rmp.notification.infrastructure.persistence;

import com.cuea.rmp.notification.domain.DeviceToken;
import org.springframework.stereotype.Component;

@Component
public class DeviceTokenMapper {

    public DeviceToken toDomain(DeviceTokenJpaEntity entity) {
        return DeviceToken.reconstitute(
                entity.getId(), entity.getUserId(), entity.getFcmToken(), entity.getPlatform());
    }

    public DeviceTokenJpaEntity toNewEntity(DeviceToken token) {
        DeviceTokenJpaEntity entity = new DeviceTokenJpaEntity();
        entity.setId(token.getId());
        entity.setFcmToken(token.getFcmToken());
        copyMutable(entity, token);
        return entity;
    }

    public void updateEntity(DeviceTokenJpaEntity entity, DeviceToken token) {
        copyMutable(entity, token);
    }

    private void copyMutable(DeviceTokenJpaEntity entity, DeviceToken token) {
        entity.setUserId(token.getUserId());
        entity.setPlatform(token.getPlatform());
    }
}
