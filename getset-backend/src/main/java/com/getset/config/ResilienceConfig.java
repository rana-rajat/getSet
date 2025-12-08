package com.getset.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4J Configuration for handling faults and failures
 * Implements circuit breaker, retry, and timeout patterns
 */
@Slf4j
@Configuration
public class ResilienceConfig {

    /**
     * Retry configuration for transient failures
     * - Max attempts: 3
     * - Initial interval: 100ms
     * - Multiplier: 2 (exponential backoff)
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(100, 2))
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("Retry registered: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("Retry removed: {}", event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Circuit Breaker configuration for preventing cascading failures
     * - Failure threshold: 50%
     * - Slow call rate threshold: 100%
     * - Slow call duration threshold: 2 seconds
     * - Wait duration in open state: 30 seconds
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(100.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("Circuit breaker registered: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("Circuit breaker removed: {}", event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Time Limiter configuration for request timeouts
     * - Timeout duration: 5 seconds
     * - Cancel running future: true
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("Time limiter registered: {}", event.getAddedEntry().getName()))
                .onEntryRemoved(event -> log.info("Time limiter removed: {}", event.getRemovedEntry().getName()));

        return registry;
    }

    /**
     * Property Repository Retry Bean
     */
    @Bean
    public Retry propertyRepositoryRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("propertyRepository");
    }

    /**
     * Property Repository Circuit Breaker Bean
     */
    @Bean
    public CircuitBreaker propertyRepositoryCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("propertyRepository");
    }

    /**
     * User Repository Retry Bean
     */
    @Bean
    public Retry userRepositoryRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("userRepository");
    }

    /**
     * User Repository Circuit Breaker Bean
     */
    @Bean
    public CircuitBreaker userRepositoryCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("userRepository");
    }

    /**
     * Auth Service Time Limiter Bean
     */
    @Bean
    public TimeLimiter authServiceTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter("authService");
    }

    /**
     * Property Service Time Limiter Bean
     */
    @Bean
    public TimeLimiter propertyServiceTimeLimiter(TimeLimiterRegistry timeLimiterRegistry) {
        return timeLimiterRegistry.timeLimiter("propertyService");
    }
}
