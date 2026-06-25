package com.cuea.rmp.bootstrap;

import com.cuea.rmp.project.application.dto.CreateProjectCommand;
import com.cuea.rmp.project.application.port.in.CreateProjectUseCase;
import com.cuea.rmp.resource.application.dto.AddSkillCommand;
import com.cuea.rmp.resource.application.dto.CreateResourceCommand;
import com.cuea.rmp.resource.application.dto.CreateSkillCommand;
import com.cuea.rmp.resource.application.dto.ResourceResult;
import com.cuea.rmp.resource.application.dto.SkillResult;
import com.cuea.rmp.resource.application.port.in.AddSkillToResourceUseCase;
import com.cuea.rmp.resource.application.port.in.CreateResourceUseCase;
import com.cuea.rmp.resource.application.port.in.CreateSkillUseCase;
import com.cuea.rmp.resource.domain.ResourceType;
import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.port.in.CreateUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DEV ONLY. Seeds a demo-ready dataset on startup: one organisation, one user per
 * role, a few skills/resources/projects. Idempotent — skips if the admin already
 * exists. Remove (or gate) before production.
 */
@Component
@Profile("dev")
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String ADMIN_EMAIL = "admin@cuea.edu";

    private final UserRepository userRepository;
    private final CreateUserUseCase createUser;
    private final CreateSkillUseCase createSkill;
    private final CreateResourceUseCase createResource;
    private final AddSkillToResourceUseCase addSkillToResource;
    private final CreateProjectUseCase createProject;

    public DataSeeder(UserRepository userRepository,
                      CreateUserUseCase createUser,
                      CreateSkillUseCase createSkill,
                      CreateResourceUseCase createResource,
                      AddSkillToResourceUseCase addSkillToResource,
                      CreateProjectUseCase createProject) {
        this.userRepository = userRepository;
        this.createUser = createUser;
        this.createSkill = createSkill;
        this.createResource = createResource;
        this.addSkillToResource = addSkillToResource;
        this.createProject = createProject;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(Email.of(ADMIN_EMAIL))) {
            return;
        }

        // --- Users (one per role) ---
        UUID adminId = createUser.create(new CreateUserCommand(
                ORG_ID, "Seed Admin", ADMIN_EMAIL, "Admin123!", Role.ADMIN)).id();
        UUID managerId = createUser.create(new CreateUserCommand(
                ORG_ID, "Maya Manager", "manager@cuea.edu", "Manager123!", Role.MANAGER)).id();
        createUser.create(new CreateUserCommand(
                ORG_ID, "Amy Approver", "approver@cuea.edu", "Approve123", Role.APPROVER));
        createUser.create(new CreateUserCommand(
                ORG_ID, "Mike Member", "member@cuea.edu", "Member123", Role.MEMBER));

        // --- Skills ---
        SkillResult java = createSkill.create(new CreateSkillCommand(ORG_ID, "Java"));
        SkillResult pm = createSkill.create(new CreateSkillCommand(ORG_ID, "Project Management"));
        SkillResult ux = createSkill.create(new CreateSkillCommand(ORG_ID, "UX Design"));

        // --- Resources (with skills) ---
        ResourceResult alice = createResource.create(new CreateResourceCommand(
                ORG_ID, null, "Alice (Engineer)", ResourceType.HUMAN, new BigDecimal("75.00"), "USD"));
        addSkillToResource.addSkill(new AddSkillCommand(alice.id(), java.id(), 5));

        ResourceResult bob = createResource.create(new CreateResourceCommand(
                ORG_ID, null, "Bob (Designer)", ResourceType.HUMAN, new BigDecimal("60.00"), "USD"));
        addSkillToResource.addSkill(new AddSkillCommand(bob.id(), ux.id(), 4));

        ResourceResult carol = createResource.create(new CreateResourceCommand(
                ORG_ID, null, "Carol (Lead)", ResourceType.HUMAN, new BigDecimal("90.00"), "USD"));
        addSkillToResource.addSkill(new AddSkillCommand(carol.id(), pm.id(), 5));
        addSkillToResource.addSkill(new AddSkillCommand(carol.id(), java.id(), 3));

        createResource.create(new CreateResourceCommand(
                ORG_ID, null, "Lab Workstation", ResourceType.EQUIPMENT, new BigDecimal("10.00"), "USD"));

        // --- Projects (managed by the manager) ---
        createProject.create(new CreateProjectCommand(
                ORG_ID, managerId, "Apollo Platform", "Flagship build",
                LocalDate.now(), LocalDate.now().plusMonths(3)));
        createProject.create(new CreateProjectCommand(
                ORG_ID, managerId, "Hermes Mobile", "Companion app",
                LocalDate.now(), LocalDate.now().plusMonths(2)));

        log.warn("[DEV] Seeded demo data: org={}, admin={} (admin id {}). Remove DataSeeder before production.",
                ORG_ID, ADMIN_EMAIL, adminId);
    }
}
