package com.cuea.rmp.user.infrastructure.persistence;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the {@link UserRepository} outbound port over Spring Data.
 * Reads exclude soft-deleted rows; writes go through the domain aggregate.
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = jpaRepository.findById(user.getId())
                .map(existing -> {
                    mapper.updateEntity(existing, user);
                    return existing;
                })
                .orElseGet(() -> mapper.toNewEntity(user));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findByIdAndDeletedFalse(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmailAndDeletedFalse(email.value()).map(mapper::toDomain);
    }

    @Override
    public PageResult<User> findAll(int page, int size) {
        Page<UserJpaEntity> result = jpaRepository.findAllByDeletedFalse(PageRequest.of(page, size));
        return PageResult.of(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmailAndDeletedFalse(email.value());
    }
}
