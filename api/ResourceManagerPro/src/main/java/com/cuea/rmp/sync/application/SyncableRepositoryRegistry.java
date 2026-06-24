package com.cuea.rmp.sync.application;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.sync.application.port.out.SyncableRepository;
import com.cuea.rmp.sync.domain.EntityType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Maps each {@link EntityType} to its {@link SyncableRepository}, making the engine generic. */
@Component
public class SyncableRepositoryRegistry {

    private final Map<EntityType, SyncableRepository> byType = new EnumMap<>(EntityType.class);

    public SyncableRepositoryRegistry(List<SyncableRepository> repositories) {
        for (SyncableRepository repository : repositories) {
            byType.put(repository.entityType(), repository);
        }
    }

    public SyncableRepository get(EntityType entityType) {
        SyncableRepository repository = byType.get(entityType);
        if (repository == null) {
            throw new BusinessRuleException("Unsupported entityType: " + entityType, "UNSUPPORTED_ENTITY_TYPE");
        }
        return repository;
    }

    public Collection<SyncableRepository> all() {
        return byType.values();
    }
}
