package com.getset.property.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lightweight JWT filter — validates the token and sets authentication.
 * Unlike user-service, this doesn't load from DB — it reads claims from the
 * token directly.
 * The username (email) stored in the JWT is used as the principal.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = authHeader.substring(7);
        try {
            String username = jwtService.extractUsername(jwt);
            String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authorities = role != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        : List.<SimpleGrantedAuthority>of();
                var authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ignored) {
        }
        filterChain.doFilter(request, response);
    }
}
