package com.getset.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GetSet API Gateway
 *
 * Acts as the single entry point for all clients (browser, mobile app).
 * Responsibilities:
 * - Route requests to the getset-backend service
 * - Enforce rate limiting at the network edge
 * - Circuit-break routes to isolated backend failures
 * - Propagate trace IDs (X-B3-TraceId) for Zipkin correlation
 * - Handle CORS centrally (removes need for it in backend services)
 *
 * Architecture:
 * Client → :8090 (API Gateway) → :8080 (getset-backend monolith)
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
