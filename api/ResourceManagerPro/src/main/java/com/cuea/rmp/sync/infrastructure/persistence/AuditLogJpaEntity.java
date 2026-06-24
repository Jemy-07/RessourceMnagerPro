package com.cuea.rmp.sync.infrastructure.persistence;

import com.cuea.rmp.shared.domain.AuditAction;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogJpaEntity extends BaseJpaEntity {

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "entity_id", length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "conflict", nullable = false)
    private boolean conflict;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
