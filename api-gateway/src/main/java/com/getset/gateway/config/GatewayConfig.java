package com.getset.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * API Gateway routing — each path prefix routes to the corresponding
 * microservice.
 *
 * /api/v1/auth/** → user-service:8081
 * /api/v1/users/** → user-service:8081
 * /api/v1/properties/** → property-service:8082
 * /api/v1/enquiries/** → enquiry-service:8083
 * /api/v1/messages/** → message-service:8084
 * /api/v1/notifications/** → notification-service:8085
 * /api/v1/favorites/** → favorite-service:8086
 */
@Configuration
public class GatewayConfig {

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.property-service.url:http://localhost:8082}")
    private String propertyServiceUrl;

    @Value("${services.enquiry-service.url:http://localhost:8083}")
    private String enquiryServiceUrl;

    @Value("${services.message-service.url:http://localhost:8084}")
    private String messageServiceUrl;

    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;

    @Value("${services.favorite-service.url:http://localhost:8086}")
    private String favoriteServiceUrl;

    @Value("${gateway.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsStr;

    @Bean
    public RedisRateLimiter authRateLimiter() {
        return new RedisRateLimiter(20, 10, 60);
    }

    @Bean
    public RedisRateLimiter apiRateLimiter() {
        return new RedisRateLimiter(60, 20, 60);
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown");
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── User Service ─────────────────────────────────────────────────
                .route("user-service-auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(authRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("user-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/auth")))
                        .uri(userServiceUrl))

                // ── Property Service ────────────────────────────────────────────
                .route("property-service", r -> r
                        .path("/api/v1/properties/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("property-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(propertyServiceUrl))

                // ── Enquiry Service ─────────────────────────────────────────────
                .route("enquiry-service", r -> r
                        .path("/api/v1/enquiries/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("enquiry-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(enquiryServiceUrl))

                // ── Message Service ─────────────────────────────────────────────
                .route("message-service", r -> r
                        .path("/api/v1/messages/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("message-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(messageServiceUrl))

                // ── Notification Service ────────────────────────────────────────
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("notification-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(notificationServiceUrl))

                // ── Favorite Service ────────────────────────────────────────────
                .route("favorite-service", r -> r
                        .path("/api/v1/favorites/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Request-Id", UUID.randomUUID().toString())
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(apiRateLimiter()).setKeyResolver(ipKeyResolver()))
                                .circuitBreaker(cb -> cb
                                        .setName("favorite-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/service")))
                        .uri(favoriteServiceUrl))

                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOriginsStr.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE,
                "X-Requested-With", "X-Gateway-Request-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
