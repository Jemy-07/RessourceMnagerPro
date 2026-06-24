package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.resource.application.port.out.ResourceRepository;
import com.cuea.rmp.resource.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ResourcePersistenceAdapter implements ResourceRepository {

    private final ResourceJpaRepository jpaRepository;
    private final ResourceMapper mapper;

    public ResourcePersistenceAdapter(ResourceJpaRepository jpaRepository, ResourceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Resource save(Resource resource) {
        ResourceJpaEntity entity = jpaRepository.findById(resource.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, resource);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(resource));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Resource> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Resource> findAll(int page, int size) {
        Page<ResourceJpaEntity> result = jpaRepository.findAllByDeletedFalse(PageRequest.of(page, size));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public List<Resource> findBySkill(UUID skillId) {
        return jpaRepository.findBySkillId(skillId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void softDelete(UUID id) {
        jpaRepository.findByIdAndDeletedFalse(id).ifPresent(entity -> {
            entity.setDeleted(true);
            jpaRepository.save(entity);
        });
    }
}
