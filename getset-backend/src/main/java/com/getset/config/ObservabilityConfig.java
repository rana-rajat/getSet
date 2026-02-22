package com.getset.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observability configuration for the GetSet backend.
 *
 * Registers global metric tags (application, environment, version) and
 * declares custom business counters for Prometheus/Grafana dashboards.
 *
 * Metrics exposed at: GET /api/v1/actuator/prometheus
 * Zipkin traces sent to: ${ZIPKIN_URL}/api/v2/spans
 */
@Configuration
public class ObservabilityConfig {

    @Value("${spring.application.name:getset-backend}")
    private String appName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    // ── Global Tags ───────────────────────────────────────────────────────────

    /**
     * Adds application-level tags to ALL metrics automatically.
     * These appear as label dimensions in Prometheus and Grafana queries.
     * e.g. http_server_requests_seconds{application="getset-backend", env="prod"}
     * ...
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> globalMetricsTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", appName,
                        "env", activeProfile,
                        "version", "1.0.0")
                .meterFilter(MeterFilter.deny(id ->
                // Suppress JVM classloading metrics (not useful in dashboards)
                id.getName().startsWith("jvm.classes")));
    }

    // ── Business Counters ─────────────────────────────────────────────────────

    /** Counter: total enquiries created */
    @Bean
    public Counter enquiriesCreatedCounter(MeterRegistry registry) {
        return Counter.builder("getset.enquiries.created")
                .description("Total number of property enquiries created")
                .register(registry);
    }

    /** Counter: total enquiries accepted */
    @Bean
    public Counter enquiriesAcceptedCounter(MeterRegistry registry) {
        return Counter.builder("getset.enquiries.accepted")
                .description("Total number of enquiries accepted by owners")
                .register(registry);
    }

    /** Counter: total enquiries rejected */
    @Bean
    public Counter enquiriesRejectedCounter(MeterRegistry registry) {
        return Counter.builder("getset.enquiries.rejected")
                .description("Total number of enquiries rejected by owners")
                .register(registry);
    }

    /** Counter: total messages sent */
    @Bean
    public Counter messagesSentCounter(MeterRegistry registry) {
        return Counter.builder("getset.messages.sent")
                .description("Total number of messages sent between users")
                .register(registry);
    }

    /** Counter: total user registrations */
    @Bean
    public Counter userRegistrationsCounter(MeterRegistry registry) {
        return Counter.builder("getset.users.registered")
                .description("Total number of new user registrations")
                .register(registry);
    }

    /** Counter: total property listings created */
    @Bean
    public Counter propertiesCreatedCounter(MeterRegistry registry) {
        return Counter.builder("getset.properties.created")
                .description("Total number of property listings created")
                .register(registry);
    }
}
