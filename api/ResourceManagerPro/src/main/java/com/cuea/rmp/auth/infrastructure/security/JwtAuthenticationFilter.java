package com.cuea.rmp.auth.infrastructure.security;

import com.cuea.rmp.auth.application.dto.TokenClaims;
import com.cuea.rmp.auth.application.port.out.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads a {@code Bearer} access token, validates it, and populates the
 * SecurityContext with an {@link AuthenticatedUser} principal and a
 * {@code ROLE_<role>} authority. Invalid tokens leave the context empty so the
 * authorization rules / entry point produce a 401.
 * <p>
 * Deliberately NOT a Spring bean — it is wired manually in {@code SecurityConfig}
 * to avoid Boot auto-registering it as a servlet filter for every request.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                TokenClaims claims = tokenProvider.parseAccessToken(token);
                AuthenticatedUser principal = new AuthenticatedUser(
                        claims.userId(), claims.role(), claims.orgId(), claims.email());
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(request.getRequestURI());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ex) {
                // Invalid token → stay anonymous; protected routes will 401.
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
