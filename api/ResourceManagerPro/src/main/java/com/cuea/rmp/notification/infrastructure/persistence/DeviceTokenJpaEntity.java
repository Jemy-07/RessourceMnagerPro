package com.cuea.rmp.notification.infrastructure.persistence;

import com.cuea.rmp.notification.domain.Platform;
import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "device_tokens")
@Getter
@Setter
@NoArgsConstructor
public class DeviceTokenJpaEntity extends BaseJpaEntity {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private Platform platform;
}
