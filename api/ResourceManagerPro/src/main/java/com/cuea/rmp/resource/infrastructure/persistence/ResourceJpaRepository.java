package com.cuea.rmp.resource.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceJpaRepository extends JpaRepository<ResourceJpaEntity, UUID> {

    Optional<ResourceJpaEntity> findByIdAndDeletedFalse(UUID id);

    Page<ResourceJpaEntity> findAllByDeletedFalse(Pageable pageable);

    @Query("select distinct r from ResourceJpaEntity r join r.skills s "
            + "where s.skillId = :skillId and r.deleted = false")
    List<ResourceJpaEntity> findBySkillId(@Param("skillId") UUID skillId);
}
