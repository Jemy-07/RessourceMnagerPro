package com.cuea.rmp.user.application.port.out;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for the User aggregate. Implementations must operate
 * only on non-deleted rows (soft-delete filter) and treat the domain {@link User}
 * as the source of truth.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    PageResult<User> findAll(int page, int size);

    boolean existsByEmail(Email email);
}
