package com.cuea.rmp.user.application.usecase;

import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.user.application.port.in.DeactivateUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeactivateUserService implements DeactivateUserUseCase {

    private final UserRepository userRepository;

    public DeactivateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void deactivate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        user.deactivate();
        userRepository.save(user);
    }
}
