package com.cuea.rmp.user.application.usecase;

import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.user.application.dto.UpdateUserCommand;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.UpdateUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResult update(UpdateUserCommand command) {
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("User " + command.id() + " not found"));
        user.rename(command.fullName());
        user.changeRole(command.role());
        return UserResult.from(userRepository.save(user));
    }
}
