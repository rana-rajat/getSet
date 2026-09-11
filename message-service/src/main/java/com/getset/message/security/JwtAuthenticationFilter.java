package com.getset.message.security;

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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String jwt = header.substring(7);
                String username = jwtService.extractUsername(jwt);
                String role = jwtService.extractClaim(jwt, c -> c.get("role", String.class));
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var auth = new UsernamePasswordAuthenticationToken(username, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "RENTER"))));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(req, res);
    }
}
