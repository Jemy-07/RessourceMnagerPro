package com.cuea.rmp.auth.infrastructure.security;

import com.cuea.rmp.auth.application.dto.AuthTokens;
import com.cuea.rmp.auth.application.dto.TokenClaims;
import com.cuea.rmp.auth.application.port.out.TokenProvider;
import com.cuea.rmp.shared.domain.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** JWT (jjwt 0.12.x) implementation of {@link TokenProvider} using HMAC-SHA signing. */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_ORG = "orgId";
    private static final String CLAIM_EMAIL = "email";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTtl = Duration.ofMinutes(properties.accessTokenTtlMinutes());
        this.refreshTtl = Duration.ofDays(properties.refreshTokenTtlDays());
        this.clock = clock;
    }

    @Override
    public AuthTokens issueTokens(UUID userId, String role, UUID orgId, String email) {
        Instant now = Instant.now(clock);
        Instant accessExp = now.plus(accessTtl);
        Instant refreshExp = now.plus(refreshTtl);
        String refreshId = UUID.randomUUID().toString();

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_ORG, orgId.toString())
                .claim(CLAIM_EMAIL, email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExp))
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(userId.toString())
                .id(refreshId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExp))
                .signWith(key)
                .compact();

        return new AuthTokens(accessToken, refreshToken, accessTtl.toSeconds(), refreshId, refreshExp);
    }

    @Override
    public TokenClaims parseAccessToken(String token) {
        Claims claims = parse(token, TYPE_ACCESS);
        return new TokenClaims(
                UUID.fromString(claims.getSubject()),
                claims.get(CLAIM_ROLE, String.class),
                UUID.fromString(claims.get(CLAIM_ORG, String.class)),
                claims.get(CLAIM_EMAIL, String.class),
                null);
    }

    @Override
    public TokenClaims parseRefreshToken(String token) {
        Claims claims = parse(token, TYPE_REFRESH);
        return new TokenClaims(
                UUID.fromString(claims.getSubject()),
                null, null, null,
                claims.getId());
    }

    private Claims parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new UnauthorizedException("Wrong token type", "INVALID_TOKEN");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid or expired token", "INVALID_TOKEN");
        }
    }
}
