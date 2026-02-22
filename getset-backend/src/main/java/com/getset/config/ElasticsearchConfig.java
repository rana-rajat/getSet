package com.getset.config;

import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch Configuration
 * Spring Boot auto-configures from application.yml properties:
 * - spring.elasticsearch.uris
 * - spring.elasticsearch.socket-timeout
 * - spring.elasticsearch.connection-timeout
 */
@Configuration
public class ElasticsearchConfig {
    // Spring Boot 3.4 auto-configures Elasticsearch client from application.yml
    // No additional configuration needed - RestClientConfiguration is auto-enabled
}

