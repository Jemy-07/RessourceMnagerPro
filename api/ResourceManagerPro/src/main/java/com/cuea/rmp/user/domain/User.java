package com.cuea.rmp.user.domain;

import com.cuea.rmp.shared.domain.BusinessRuleException;

import java.util.Objects;
import java.util.UUID;

/**
 * User aggregate root. Pure Java — no Spring, JPA, or web concerns.
 * <p>
 * Identity ({@link UUID}) is generated here in the domain. Construction goes
 * through {@link #create} (new users) or {@link #reconstitute} (rehydration from
 * persistence). Behaviour methods enforce invariants.
 */
public class User {

    private final UUID id;
    private final UUID orgId;
    private String fullName;
    private final Email email;
    private String passwordHash;
    private Role role;
    private boolean active;

    private User(UUID id, UUID orgId, String fullName, Email email,
                 String passwordHash, Role role, boolean active) {
        this.id = id;
        this.orgId = orgId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    /** Create a brand-new, active user with a freshly generated id. */
    public static User create(UUID orgId, String fullName, Email email, String passwordHash, Role role) {
        requireOrgId(orgId);
        requireName(fullName);
        Objects.requireNonNull(email, "email must not be null");
        requirePasswordHash(passwordHash);
        Objects.requireNonNull(role, "role must not be null");
        return new User(UUID.randomUUID(), orgId, fullName.trim(), email, passwordHash, role, true);
    }

    /** Rebuild an existing user from stored state (no invariant generation). */
    public static User reconstitute(UUID id, UUID orgId, String fullName, Email email,
                                    String passwordHash, Role role, boolean active) {
        return new User(
                Objects.requireNonNull(id, "id must not be null"),
                orgId, fullName, email, passwordHash, role, active);
    }

    public void rename(String newFullName) {
        requireName(newFullName);
        this.fullName = newFullName.trim();
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole, "role must not be null");
    }

    public void deactivate() {
        if (!active) {
            throw new BusinessRuleException("User is already inactive", "USER_ALREADY_INACTIVE");
        }
        this.active = false;
    }

    private static void requireOrgId(UUID orgId) {
        if (orgId == null) {
            throw new BusinessRuleException("orgId must not be null", "INVALID_USER");
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("fullName must not be blank", "INVALID_USER");
        }
    }

    private static void requirePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessRuleException("passwordHash must not be blank", "INVALID_USER");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
