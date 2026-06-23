package com.cuea.rmp.user.application.usecase;

import com.cuea.rmp.shared.application.PageResult;
import com.cuea.rmp.user.application.dto.UserResult;
import com.cuea.rmp.user.application.port.in.ListUsersUseCase;
import com.cuea.rmp.user.application.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersUseCase {

    private final UserRepository userRepository;

    public ListUsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageResult<UserResult> list(int page, int size) {
        PageResult<com.cuea.rmp.user.domain.User> users = userRepository.findAll(page, size);
        return new PageResult<>(
                users.content().stream().map(UserResult::from).toList(),
                users.page(),
                users.size(),
                users.totalElements(),
                users.totalPages());
    }
}
