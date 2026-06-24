package com.cuea.rmp.auth.application.usecase;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.LoginCommand;
import com.cuea.rmp.auth.application.port.in.LoginUseCase;
import com.cuea.rmp.auth.application.port.out.RefreshTokenStore;
import com.cuea.rmp.auth.application.port.out.TokenProvider;
import com.cuea.rmp.shared.application.PasswordHasher;
import com.cuea.rmp.shared.domain.UnauthorizedException;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.Email;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public LoginService(UserRepository userRepository,
                        PasswordHasher passwordHasher,
                        TokenProvider tokenProvider,
                        RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public AuthTokens login(LoginCommand command) {
        // Uniform error message to avoid leaking which part failed.
        User user = userRepository.findByEmail(Email.of(command.email()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive", "ACCOUNT_INACTIVE");
        }
        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS");
        }

        AuthTokens tokens = tokenProvider.issueTokens(
                user.getId(), user.getRole().name(), user.getOrgId(), user.getEmail().value());
        refreshTokenStore.store(user.getId(), tokens.refreshTokenId(), tokens.refreshExpiresAt());
        return tokens;
    }
}
