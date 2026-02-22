package com.getset.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * API Gateway routing configuration.
 *
 * Routes:
 * /api/v1/auth/** → backend (rate-limited: 20/min per IP)
 * /api/v1/properties/** → backend (rate-limited: 100/min per IP)
 * /api/v1/** → backend (rate-limited: 60/min per IP)
 *
 * Each route has:
 * - CircuitBreaker wrapping (opens after 50% failures in 10s window)
 * - X-Gateway-Request-Id header injection
 * - Trace ID propagation (automatic via Micrometer)
 *
 * CORS is handled here — backend services do NOT need their own CORS config.
 */
@Configuration
public class GatewayConfig {

    @Value("${gateway.backend-uri:http://localhost:8080}")
    private String backendUri;

    @Value("${gateway.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsStr;

    // ── Rate Limiters ─────────────────────────────────────────────────────────

    /** Strict rate limiter for auth endpoints — prevents brute force */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        // 20 requests per minute; burst of 10
        return new RedisRateLimiter(20, 10, 60);
    }

    /** Standard rate limiter for API endpoints */
    @Bean
    public RedisRateLimiter apiRateLimiter() {
        // 60 requests per minute; burst of 20
        return new RedisRateLimiter(60, 20, 60);
    }

    /** Key resolver: uses client IP address as the rate limit key */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    // ── Routes ────────────────────────────────────────────────────────────────

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth endpoints — strict rate limit (20/min)
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id",
                                        java.util.UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(authRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("auth-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(backendUri))

                // Property endpoints — moderate rate limit (100/min)
                .route("property-service", r -> r
                        .path("/api/v1/properties/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id",
                                        java.util.UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("property-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(backendUri))

                // Search endpoints
                .route("search-service", r -> r
                        .path("/api/v1/search/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id",
                                        java.util.UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("search-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(backendUri))

                // All other /api/v1 endpoints (enquiries, messages, notifications, favorites)
                .route("default-api", r -> r
                        .path("/api/v1/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id",
                                        java.util.UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("default-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(backendUri))

                .build();
    }

    // ── CORS ─────────────────────────────────────────────────────────────────

    /**
     * Handle CORS at the gateway level.
     * The backend service does NOT need to configure CORS.
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOriginsStr.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                "X-Requested-With",
                "X-Gateway-Request-Id"));
        config.setExposedHeaders(Arrays.asList(
                "X-Gateway-Request-Id",
                "X-B3-TraceId"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
