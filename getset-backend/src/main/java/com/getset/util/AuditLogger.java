package com.getset.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for structured audit and security event logging
 * Follows enterprise logging standards with required fields
 */
@Slf4j
@Component
public class AuditLogger {

    /**
     * Log authentication attempt
     */
    public void logAuthenticationAttempt(String email, boolean success, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "AUTHENTICATION_ATTEMPT");
        event.put("email", maskEmail(email));
        event.put("success", success);
        event.put("timestamp", Instant.now());
        event.put("reason", reason);
        
        if (success) {
            log.info("Authentication successful: {}", event);
        } else {
            log.warn("Authentication failed: {}", event);
        }
    }

    /**
     * Log registration attempt
     */
    public void logRegistrationAttempt(String email, String role, boolean success, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_REGISTRATION");
        event.put("email", maskEmail(email));
        event.put("role", role);
        event.put("success", success);
        event.put("timestamp", Instant.now());
        event.put("reason", reason);
        
        if (success) {
            log.info("User registration successful: {}", event);
        } else {
            log.warn("User registration failed: {}", event);
        }
    }

    /**
     * Log authorization failure
     */
    public void logAuthorizationFailure(String userId, String resourceId, String action) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "AUTHORIZATION_FAILURE");
        event.put("userId", userId);
        event.put("resourceId", resourceId);
        event.put("action", action);
        event.put("timestamp", Instant.now());
        
        log.warn("Authorization failure: {}", event);
    }

    /**
     * Log resource access
     */
    public void logResourceAccess(String userId, String resourceType, String resourceId, String action) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "RESOURCE_ACCESS");
        event.put("userId", userId);
        event.put("resourceType", resourceType);
        event.put("resourceId", resourceId);
        event.put("action", action);
        event.put("timestamp", Instant.now());
        
        log.info("Resource access: {}", event);
    }

    /**
     * Log exception with security context
     */
    public void logException(String context, Exception ex, String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "EXCEPTION");
        event.put("context", context);
        event.put("userId", userId);
        event.put("exceptionType", ex.getClass().getSimpleName());
        event.put("message", ex.getMessage());
        event.put("timestamp", Instant.now());
        
        log.error("Exception occurred: {}", event, ex);
    }

    /**
     * Log data modification
     */
    public void logDataModification(String userId, String entityType, String entityId, String action) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "DATA_MODIFICATION");
        event.put("userId", userId);
        event.put("entityType", entityType);
        event.put("entityId", entityId);
        event.put("action", action);
        event.put("timestamp", Instant.now());
        
        log.info("Data modification: {}", event);
    }

    /**
     * Mask email for security (SEC-01 compliance)
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex - 1);
    }
}
