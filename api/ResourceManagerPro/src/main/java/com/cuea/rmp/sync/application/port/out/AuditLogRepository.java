package com.cuea.rmp.sync.application.port.out;

import com.cuea.rmp.sync.domain.AuditLog;

import java.util.List;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findConflicts();
}
