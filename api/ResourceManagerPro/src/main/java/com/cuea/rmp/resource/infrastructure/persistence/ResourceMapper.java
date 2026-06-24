package com.cuea.rmp.resource.infrastructure.persistence;

import com.cuea.rmp.resource.domain.Resource;
import com.cuea.rmp.resource.domain.ResourceSkill;
import com.cuea.rmp.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Maps between the {@link Resource} aggregate and its JPA entity, reconciling skills. */
@Component
public class ResourceMapper {

    public Resource toDomain(ResourceJpaEntity entity) {
        List<ResourceSkill> skills = entity.getSkills().stream()
                .map(s -> ResourceSkill.reconstitute(s.getId(), entity.getId(), s.getSkillId(), s.getProficiency()))
                .toList();
        return Resource.reconstitute(
                entity.getId(),
                entity.getOrgId(),
                entity.getUserId(),
                entity.getName(),
                entity.getType(),
                Money.of(entity.getHourlyRateAmount(), entity.getHourlyRateCurrency()),
                entity.getAvailabilityStatus(),
                skills);
    }

    public ResourceJpaEntity toNewEntity(Resource resource) {
        ResourceJpaEntity entity = new ResourceJpaEntity();
        entity.setId(resource.getId());
        entity.setOrgId(resource.getOrgId());
        copyScalars(entity, resource);
        syncSkills(entity, resource);
        return entity;
    }

    public void updateEntity(ResourceJpaEntity entity, Resource resource) {
        copyScalars(entity, resource);
        syncSkills(entity, resource);
    }

    private void copyScalars(ResourceJpaEntity entity, Resource resource) {
        entity.setUserId(resource.getUserId());
        entity.setName(resource.getName());
        entity.setType(resource.getType());
        entity.setHourlyRateAmount(resource.getHourlyRate().getAmount());
        entity.setHourlyRateCurrency(resource.getHourlyRate().getCurrency());
        entity.setAvailabilityStatus(resource.getAvailabilityStatus());
    }

    /** Add new skills, update changed proficiencies, and remove ones no longer present. */
    private void syncSkills(ResourceJpaEntity entity, Resource resource) {
        Map<UUID, ResourceSkillJpaEntity> existing = entity.getSkills().stream()
                .collect(Collectors.toMap(ResourceSkillJpaEntity::getSkillId, s -> s, (a, b) -> a, HashMap::new));
        Set<UUID> domainSkillIds = resource.getSkills().stream()
                .map(ResourceSkill::getSkillId).collect(Collectors.toSet());

        entity.getSkills().removeIf(s -> !domainSkillIds.contains(s.getSkillId()));

        for (ResourceSkill domainSkill : resource.getSkills()) {
            ResourceSkillJpaEntity child = existing.get(domainSkill.getSkillId());
            if (child == null) {
                child = new ResourceSkillJpaEntity();
                child.setId(domainSkill.getId());
                child.setResource(entity);
                child.setSkillId(domainSkill.getSkillId());
                child.setProficiency(domainSkill.getProficiency());
                entity.getSkills().add(child);
            } else {
                child.setProficiency(domainSkill.getProficiency());
            }
        }
    }
}
