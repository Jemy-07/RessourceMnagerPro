package com.cuea.rmp.sync.application.usecase;

import com.cuea.rmp.shared.domain.AuditAction;
import com.cuea.rmp.sync.application.SyncableRepositoryRegistry;
import com.cuea.rmp.sync.application.dto.ConflictInfo;
import com.cuea.rmp.sync.application.dto.PushResult;
import com.cuea.rmp.sync.application.dto.SyncRow;
import com.cuea.rmp.sync.application.port.in.PushChangesUseCase;
import com.cuea.rmp.sync.application.port.out.AuditLogRepository;
import com.cuea.rmp.sync.application.port.out.SyncableRepository;
import com.cuea.rmp.sync.domain.AuditLog;
import com.cuea.rmp.sync.domain.ChangeSet;
import com.cuea.rmp.sync.domain.SyncEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Applies a batch of client changes.
 * <p>
 * A version mismatch against the stored row signals a concurrent edit; it is
 * resolved by LAST-WRITE-WINS on {@code updatedAt}. The losing side is recorded
 * in {@link AuditLog} flagged as a conflict. Applied rows are marked SYNCED.
 */
@Service
@Transactional
public class PushChangesService implements PushChangesUseCase {

    private final SyncableRepositoryRegistry registry;
    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public PushChangesService(SyncableRepositoryRegistry registry,
                              AuditLogRepository auditLogRepository,
                              Clock clock) {
        this.registry = registry;
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    @Override
    public PushResult push(ChangeSet changeSet) {
        int applied = 0;
        List<ConflictInfo> conflicts = new ArrayList<>();

        for (SyncEntry entry : changeSet.entries()) {
            SyncableRepository repository = registry.get(entry.entityType());
            Optional<SyncRow> existing = repository.findById(entry.id());
            AuditAction action = entry.deleted() ? AuditAction.DELETE
                    : existing.isPresent() ? AuditAction.UPDATE : AuditAction.CREATE;

            boolean concurrentEdit = existing.isPresent() && entry.clientVersion() != existing.get().version();

            if (concurrentEdit) {
                SyncRow server = existing.get();
                boolean clientWins = entry.clientUpdatedAt().isAfter(server.updatedAt());
                if (clientWins) {
                    repository.upsert(entry.id(), entry.payload(), entry.deleted());
                    applied++;
                    conflicts.add(recordConflict(entry, action, "CLIENT_WON",
                            "Concurrent edit: client (v%d, %s) is newer than server (v%d, %s) — applied (last-write-wins)"
                                    .formatted(entry.clientVersion(), entry.clientUpdatedAt(),
                                            server.version(), server.updatedAt())));
                } else {
                    repository.markSynced(entry.id());
                    conflicts.add(recordConflict(entry, action, "SERVER_WON",
                            "Concurrent edit: server (v%d, %s) is newer than client (v%d, %s) — client change rejected (last-write-wins)"
                                    .formatted(server.version(), server.updatedAt(),
                                            entry.clientVersion(), entry.clientUpdatedAt())));
                }
            } else {
                repository.upsert(entry.id(), entry.payload(), entry.deleted());
                applied++;
            }
        }
        return new PushResult(applied, conflicts.size(), conflicts);
    }

    private ConflictInfo recordConflict(SyncEntry entry, AuditAction action, String resolution, String message) {
        auditLogRepository.save(AuditLog.conflict(
                entry.entityType().name(), entry.id(), action, message, Instant.now(clock)));
        return new ConflictInfo(entry.entityType(), entry.id(), resolution, message);
    }
}
