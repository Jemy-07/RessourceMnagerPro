package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.application.port.out.TimeOffRepository;
import com.cuea.rmp.resource.domain.TimeOff;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TimeOffPersistenceAdapter implements TimeOffRepository {

    private final TimeOffJpaRepository jpaRepository;
    private final TimeOffMapper mapper;

    public TimeOffPersistenceAdapter(TimeOffJpaRepository jpaRepository, TimeOffMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TimeOff save(TimeOff timeOff) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewEntity(timeOff)));
    }

    @Override
    public List<TimeOff> findByResourceId(UUID resourceId) {
        return jpaRepository.findByResourceIdAndDeletedFalse(resourceId).stream()
                .map(mapper::toDomain).toList();
    }
}
