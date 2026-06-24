package com.cuea.rmp.auth.application.usecase;

import com.cuea.rmp.auth.application.dto.RegisterCommand;
import com.cuea.rmp.auth.application.dto.RegisterResult;
import com.cuea.rmp.auth.application.port.in.RegisterUseCase;
import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.CreateUserUseCase;
import com.cuea.rmp.user.domain.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Self-service registration. Delegates to the user feature's create use case
 * (which hashes the password and enforces email uniqueness) and always assigns
 * the {@link Role#MEMBER} role — registration cannot escalate privileges.
 */
@Service
@Transactional
public class RegisterService implements RegisterUseCase {

    private final CreateUserUseCase createUser;

    public RegisterService(CreateUserUseCase createUser) {
        this.createUser = createUser;
    }

    @Override
    public RegisterResult register(RegisterCommand command) {
        UserResult user = createUser.create(new CreateUserCommand(
                command.orgId(),
                command.fullName(),
                command.email(),
                command.password(),
                Role.MEMBER));
        return new RegisterResult(user.id(), user.email(), user.role());
    }
}
