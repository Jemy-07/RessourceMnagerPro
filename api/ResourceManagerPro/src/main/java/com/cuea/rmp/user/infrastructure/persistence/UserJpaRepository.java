package com.cuea.rmp.user.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link UserJpaEntity}. All lookups exclude
 * soft-deleted rows; {@code findById} (raw) is reserved for the save path.
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByIdAndDeletedFalse(UUID id);

    Optional<UserJpaEntity> findByEmailAndDeletedFalse(String email);

    Page<UserJpaEntity> findAllByDeletedFalse(Pageable pageable);

    boolean existsByEmailAndDeletedFalse(String email);
}
