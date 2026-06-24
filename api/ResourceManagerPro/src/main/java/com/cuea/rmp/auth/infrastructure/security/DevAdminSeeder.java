package com.cuea.rmp.auth.infrastructure.security;

import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.port.in.CreateUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * DEV ONLY. Seeds a single ADMIN account so the RBAC-protected endpoints can be
 * bootstrapped (there is otherwise no way to create the first ADMIN, since user
 * management is ADMIN-only). Idempotent — skips if the admin already exists.
 */
@Component
@Profile("dev")
public class DevAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);

    private static final UUID SEED_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String ADMIN_EMAIL = "admin@cuea.edu";
    private static final String ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final CreateUserUseCase createUser;

    public DevAdminSeeder(UserRepository userRepository, CreateUserUseCase createUser) {
        this.userRepository = userRepository;
        this.createUser = createUser;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(Email.of(ADMIN_EMAIL))) {
            return;
        }
        createUser.create(new CreateUserCommand(
                SEED_ORG_ID, "Seed Admin", ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN));
        log.warn("[DEV] Seeded ADMIN account {} / {} — remove before production", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
