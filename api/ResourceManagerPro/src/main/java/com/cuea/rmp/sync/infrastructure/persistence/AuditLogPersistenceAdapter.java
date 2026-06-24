package com.cuea.rmp.sync.infrastructure.persistence;

import com.cuea.rmp.sync.application.port.out.AuditLogRepository;
import com.cuea.rmp.sync.domain.AuditLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditLogPersistenceAdapter implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    public AuditLogPersistenceAdapter(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setId(auditLog.getId());
        entity.setEntityType(auditLog.getEntityType());
        entity.setEntityId(auditLog.getEntityId());
        entity.setAction(auditLog.getAction());
        entity.setConflict(auditLog.isConflict());
        entity.setMessage(auditLog.getMessage());
        entity.setOccurredAt(auditLog.getOccurredAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<AuditLog> findConflicts() {
        return jpaRepository.findByConflictTrueOrderByOccurredAtDesc().stream().map(this::toDomain).toList();
    }

    private AuditLog toDomain(AuditLogJpaEntity e) {
        return AuditLog.reconstitute(e.getId(), e.getEntityType(), e.getEntityId(),
                e.getAction(), e.isConflict(), e.getMessage(), e.getOccurredAt());
    }
}
