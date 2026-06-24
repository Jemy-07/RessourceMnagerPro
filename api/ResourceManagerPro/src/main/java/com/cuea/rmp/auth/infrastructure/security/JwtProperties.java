package com.cuea.rmp.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.jwt.*} from application.yml. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays
) {}
