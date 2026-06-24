package com.cuea.rmp.sync.infrastructure.engine;

import com.cuea.rmp.shared.domain.BusinessRuleException;
import com.cuea.rmp.shared.domain.SyncStatus;
import com.cuea.rmp.shared.infrastructure.persistence.BaseJpaEntity;
import com.cuea.rmp.sync.application.dto.SyncRow;
import com.cuea.rmp.sync.application.port.out.SyncableRepository;
import com.cuea.rmp.sync.domain.EntityType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Generic {@link SyncableRepository} over any {@link BaseJpaEntity} subtype, using
 * the EntityManager for persistence and Jackson to map payload ⇄ entity columns.
 * Base/metadata fields are server-managed and stripped from incoming payloads.
 */
public class JpaSyncableRepository implements SyncableRepository {

    private static final Set<String> BASE_FIELDS =
            Set.of("id", "createdAt", "updatedAt", "version", "syncStatus", "deleted");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final EntityType entityType;
    private final Class<? extends BaseJpaEntity> entityClass;
    private final String jpqlName;
    private final EntityManager entityManager;
    private final ObjectMapper mapper;

    public JpaSyncableRepository(EntityType entityType,
                                 Class<? extends BaseJpaEntity> entityClass,
                                 EntityManager entityManager,
                                 ObjectMapper mapper) {
        this.entityType = entityType;
        this.entityClass = entityClass;
        this.jpqlName = entityClass.getSimpleName();
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public EntityType entityType() {
        return entityType;
    }

    @Override
    public Optional<SyncRow> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(entityClass, id)).map(this::toRow);
    }

    @Override
    public List<SyncRow> findUpdatedSince(Instant since) {
        return entityManager.createQuery(
                        "select e from " + jpqlName + " e where e.updatedAt > :since order by e.updatedAt asc",
                        entityClass)
                .setParameter("since", since)
                .getResultList()
                .stream()
                .map(this::toRow)
                .toList();
    }

    @Override
    public SyncRow upsert(UUID id, Map<String, Object> payload, boolean deleted) {
        Map<String, Object> clean = new HashMap<>(payload == null ? Map.of() : payload);
        clean.keySet().removeAll(BASE_FIELDS);
        try {
            BaseJpaEntity entity = entityManager.find(entityClass, id);
            if (entity == null) {
                entity = mapper.convertValue(clean, entityClass);
                entity.setId(id);
                entity.setSyncStatus(SyncStatus.SYNCED);
                entity.setDeleted(deleted);
                entityManager.persist(entity);
            } else {
                mapper.readerForUpdating(entity).readValue(mapper.writeValueAsBytes(clean));
                entity.setSyncStatus(SyncStatus.SYNCED);
                entity.setDeleted(deleted);
            }
            entityManager.flush(); // trigger auditing (updatedAt) + version increment
            return toRow(entity);
        } catch (Exception ex) {
            throw new BusinessRuleException(
                    "Failed to apply sync change for " + entityType + " " + id + ": " + ex.getMessage(),
                    "SYNC_APPLY_FAILED");
        }
    }

    @Override
    public void markSynced(UUID id) {
        BaseJpaEntity entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entity.setSyncStatus(SyncStatus.SYNCED);
            entityManager.flush();
        }
    }

    private SyncRow toRow(BaseJpaEntity entity) {
        Map<String, Object> payload = mapper.convertValue(entity, MAP_TYPE);
        long version = entity.getVersion() == null ? 0L : entity.getVersion();
        return new SyncRow(entityType, entity.getId(), payload, entity.getUpdatedAt(), version, entity.isDeleted());
    }
}
