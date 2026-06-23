package com.cuea.rmp.user.application.usecase;

import com.cuea.rmp.shared.domain.NotFoundException;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.GetUserUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResult get(UUID id) {
        return userRepository.findById(id)
                .map(UserResult::from)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }
}
