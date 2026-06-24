package com.cuea.rmp.user.application.usecase;

import com.cuea.rmp.shared.application.PasswordHasher;
import com.cuea.rmp.shared.domain.ConflictException;
import com.cuea.rmp.user.application.dto.CreateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.CreateUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserResult create(CreateUserCommand command) {
        Email email = Email.of(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user with email " + email.value() + " already exists",
                    "EMAIL_ALREADY_EXISTS");
        }
        String passwordHash = passwordHasher.hash(command.password());

        User user = User.create(command.orgId(), command.fullName(), email, passwordHash, command.role());
        return UserResult.from(userRepository.save(user));
    }
}
