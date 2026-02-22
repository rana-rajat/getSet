package com.getset.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker routes.
 * Returns a clean JSON error instead of a raw connection refused error.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.warn("Auth service circuit breaker tripped — returning fallback response");
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message",
                        "Authentication service is temporarily unavailable. Please try again in a few seconds.",
                        "timestamp", Instant.now().toString())));
    }

    @GetMapping("/service")
    public Mono<ResponseEntity<Map<String, Object>>> serviceFallback() {
        log.warn("Backend service circuit breaker tripped — returning fallback response");
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "The service is temporarily unavailable. Please try again shortly.",
                        "timestamp", Instant.now().toString())));
    }
}
