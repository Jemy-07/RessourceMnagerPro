package com.cuea.rmp.auth.infrastructure.security;

import com.cuea.rmp.shared.domain.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Convenience access to the authenticated {@link AuthenticatedUser} from the
 * SecurityContext — exposes the current user's id, role, and orgId to use cases.
 */
@Component
public class CurrentUserProvider {

    public Optional<AuthenticatedUser> current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public AuthenticatedUser require() {
        return current().orElseThrow(
                () -> new UnauthorizedException("No authenticated user", "UNAUTHENTICATED"));
    }

    public UUID currentUserId() {
        return require().userId();
    }

    public String currentRole() {
        return require().role();
    }

    public UUID currentOrgId() {
        return require().orgId();
    }
}
