package com.cuea.rmp.sync.application.usecase;

import com.cuea.rmp.sync.application.SyncableRepositoryRegistry;
import com.cuea.rmp.sync.application.dto.PullResult;
import com.cuea.rmp.sync.application.dto.SyncRow;
import com.cuea.rmp.sync.application.port.in.PullChangesUseCase;
import com.cuea.rmp.sync.application.port.out.SyncableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Returns the delta of all syncable rows changed after {@code since}, including soft-deletes. */
@Service
@Transactional(readOnly = true)
public class PullChangesService implements PullChangesUseCase {

    private final SyncableRepositoryRegistry registry;
    private final Clock clock;

    public PullChangesService(SyncableRepositoryRegistry registry, Clock clock) {
        this.registry = registry;
        this.clock = clock;
    }

    @Override
    public PullResult pull(Instant since) {
        Instant serverTime = Instant.now(clock);
        List<SyncRow> changes = registry.all().stream()
                .map(SyncableRepository::entityType)
                .map(registry::get)
                .flatMap(repo -> repo.findUpdatedSince(since).stream())
                .sorted(Comparator.comparing(SyncRow::updatedAt))
                .toList();
        return new PullResult(serverTime, changes);
    }
}
