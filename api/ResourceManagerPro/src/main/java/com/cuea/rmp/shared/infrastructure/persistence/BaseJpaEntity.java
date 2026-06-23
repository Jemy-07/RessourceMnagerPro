package com.cuea.rmp.shared.infrastructure.persistence;

import com.cuea.rmp.shared.domain.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base for every aggregate's JPA entity.
 * <p>
 * The {@code id} is a {@link UUID} generated in the <em>domain</em> (never by JPA)
 * and persisted as {@code CHAR(36)} in MariaDB. Timestamps are populated by JPA
 * auditing; {@code version} provides optimistic locking; {@code syncStatus} and
 * {@code deleted} support offline sync and soft delete respectively.
 * <p>
 * Repositories must filter {@code deleted = false}.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
