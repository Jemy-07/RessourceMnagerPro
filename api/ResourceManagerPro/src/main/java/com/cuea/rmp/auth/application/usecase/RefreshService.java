package com.cuea.rmp.auth.application.usecase;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.RefreshCommand;
import com.cuea.rmp.auth.application.dto.TokenClaims;
import com.cuea.rmp.auth.application.port.in.RefreshUseCase;
import com.cuea.rmp.auth.application.port.out.RefreshTokenStore;
import com.cuea.rmp.auth.application.port.out.TokenProvider;
import com.cuea.rmp.shared.domain.UnauthorizedException;
import com.cuea.rmp.user.application.port.out.UserRepository;
import com.cuea.rmp.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates a refresh token against the store and rotates it: the presented
 * token is revoked and a brand-new access/refresh pair is issued and stored.
 */
@Service
@Transactional(readOnly = true)
public class RefreshService implements RefreshUseCase {

    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserRepository userRepository;

    public RefreshService(TokenProvider tokenProvider,
                          RefreshTokenStore refreshTokenStore,
                          UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.userRepository = userRepository;
    }

    @Override
    public AuthTokens refresh(RefreshCommand command) {
        TokenClaims claims = tokenProvider.parseRefreshToken(command.refreshToken());

        if (!refreshTokenStore.isValid(claims.userId(), claims.tokenId())) {
            throw new UnauthorizedException("Refresh token is no longer valid", "INVALID_REFRESH_TOKEN");
        }

        User user = userRepository.findById(claims.userId())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists", "INVALID_REFRESH_TOKEN"));
        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive", "ACCOUNT_INACTIVE");
        }

        // Rotate: invalidate the presented token, issue and store a fresh pair.
        refreshTokenStore.revoke(claims.userId(), claims.tokenId());
        AuthTokens tokens = tokenProvider.issueTokens(
                user.getId(), user.getRole().name(), user.getOrgId(), user.getEmail().value());
        refreshTokenStore.store(user.getId(), tokens.refreshTokenId(), tokens.refreshExpiresAt());
        return tokens;
    }
}
