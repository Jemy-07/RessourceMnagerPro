package com.cuea.rmp.user.infrastructure.persistence;

import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Component;

/** Maps between the {@link User} domain aggregate and {@link UserJpaEntity}. */
@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getOrgId(),
                entity.getFullName(),
                Email.of(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isActive());
    }

    /** Build a fresh JPA entity for a newly created user (audit/base fields filled by JPA). */
    public UserJpaEntity toNewEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setOrgId(user.getOrgId());
        copyMutableState(entity, user);
        return entity;
    }

    /** Copy domain state onto an already-managed entity (preserves id/version/timestamps). */
    public void updateEntity(UserJpaEntity entity, User user) {
        copyMutableState(entity, user);
    }

    private void copyMutableState(UserJpaEntity entity, User user) {
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRole(user.getRole());
        entity.setActive(user.isActive());
    }
}
