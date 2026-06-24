package com.cuea.rmp.auth.application.usecase;

import com.cuea.rmp.auth.application.dto.LogoutCommand;
import com.cuea.rmp.auth.application.dto.TokenClaims;
import com.cuea.rmp.auth.application.port.in.LogoutUseCase;
import com.cuea.rmp.auth.application.port.out.RefreshTokenStore;
import com.cuea.rmp.auth.application.port.out.TokenProvider;
import org.springframework.stereotype.Service;

/** Revokes the presented refresh token. Idempotent: a bad/expired token is a no-op. */
@Service
public class LogoutService implements LogoutUseCase {

    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public LogoutService(TokenProvider tokenProvider, RefreshTokenStore refreshTokenStore) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public void logout(LogoutCommand command) {
        try {
            TokenClaims claims = tokenProvider.parseRefreshToken(command.refreshToken());
            refreshTokenStore.revoke(claims.userId(), claims.tokenId());
        } catch (RuntimeException ignored) {
            // Logout is best-effort and idempotent — never fail on an invalid token.
        }
    }
}
