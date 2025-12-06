# 🔍 Quick Reference: Resiliency & Logging

## Resilience Annotations Quick Guide

### @Retry
```java
@Retry(name = "userRepository")
public AuthResponse login(LoginRequest request) {
    // Auto-retried 3 times with exponential backoff
    // Delays: 100ms, 200ms, 400ms
}
```
**When to use**: Database queries, remote API calls

### @CircuitBreaker  
```java
@CircuitBreaker(name = "userRepository", fallbackMethod = "loginFallback")
public AuthResponse login(LoginRequest request) {
    // Fails fast if 50% of recent calls failed
    // Waits 30 seconds before retrying
}

public AuthResponse loginFallback(LoginRequest request, Exception ex) {
    throw new RuntimeException("Service temporarily unavailable");
}
```
**When to use**: External services, database connections

### @TimeLimiter
```java
@TimeLimiter(name = "authService")
public AuthResponse register(RegisterRequest request) {
    // Must complete within 5 seconds or timeout
}
```
**When to use**: Long-running operations

## Logging Quick Guide

### Structured Audit Events
```java
@Autowired
private AuditLogger auditLogger;

// Log authentication attempt
auditLogger.logAuthenticationAttempt(email, true, "Success");

// Log registration
auditLogger.logRegistrationAttempt(email, role, true, "User created");

// Log authorization failure
auditLogger.logAuthorizationFailure(userId, resourceId, "DELETE");

// Log resource access
auditLogger.logResourceAccess(userId, "PROPERTY", propertyId, "VIEW");

// Log exception
auditLogger.logException("PropertyService", exception, userId);

// Log data modification
auditLogger.logDataModification(userId, "PROPERTY", propertyId, "DELETE");
```

### Simple Logging
```java
@Slf4j
public class MyService {
    public void doSomething() {
        log.debug("Debug message");
        log.info("Info message");
        log.warn("Warning message");
        log.error("Error message", exception);
    }
}
```

## Trace ID Usage

### How It Works
```
1. Request arrives at RequestContextFilter
   ↓
2. Generate traceId (if not in header)
   ↓
3. Add to MDC (Mapped Diagnostic Context)
   ↓
4. Every log includes traceId automatically
   ↓
5. Response includes X-Trace-ID header
```

### Accessing Trace ID
```java
String traceId = MDC.get("traceId");
String requestId = MDC.get("requestId");
String userId = MDC.get("userId");
```

### Passing to Client
```
HTTP/1.1 200 OK
X-Trace-ID: 550e8400-e29b-41d4-a716-446655440000
```

## Configuration Locations

### Resilience Settings
**File**: `src/main/resources/application.yml`
```yaml
resilience4j:
  retry:
    instances:
      userRepository:
        maxAttempts: 3
        wait-duration: 100
  
  circuitbreaker:
    instances:
      userRepository:
        failureRateThreshold: 50
        waitDurationInOpenState: 30000
  
  timelimiter:
    instances:
      authService:
        timeoutDuration: 5000
```

### Logging Settings
**File**: `src/main/resources/logback-spring.xml`
```xml
<!-- JSON Console Output -->
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>

<!-- JSON File Output -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

## Monitoring Commands

### View logs in real-time (formatted JSON)
```bash
tail -f logs/spring.log | jq .
```

### Filter by trace ID
```bash
grep "550e8400-e29b-41d4-a716-446655440000" logs/spring.log | jq .
```

### Filter by event type
```bash
grep "AUTHENTICATION_ATTEMPT" logs/spring.log | jq .
```

### Count authentication failures
```bash
grep "success.*false" logs/spring.log | wc -l
```

## Common Issues & Solutions

### Issue: Circuit breaker won't close
**Cause**: Too many failures still occurring  
**Solution**: Check database/service health, increase `waitDurationInOpenState`

### Issue: Timeout errors increasing
**Cause**: Service is too slow  
**Solution**: Increase `timeoutDuration` or optimize code

### Issue: Retry storms happening
**Cause**: Permanent error being retried  
**Solution**: Check if error should be in `ignoreExceptions` (fail-fast)

### Issue: Logs not appearing
**Cause**: Logger level too high  
**Solution**: Check `logback-spring.xml` level configuration

### Issue: High memory usage
**Cause**: Large queue of async logs  
**Solution**: Increase `queueSize` or reduce `discardingThreshold`

## Performance Tuning

### For High Throughput
```yaml
resilience4j:
  retry:
    instances:
      userRepository:
        maxAttempts: 2          # Reduce retries
        wait-duration: 50       # Shorter wait
  
  circuitbreaker:
    instances:
      userRepository:
        failureRateThreshold: 30  # Higher threshold (open faster)
```

### For High Reliability
```yaml
resilience4j:
  retry:
    instances:
      userRepository:
        maxAttempts: 5          # More retries
        wait-duration: 200      # Longer wait
  
  circuitbreaker:
    instances:
      userRepository:
        failureRateThreshold: 70  # Lower threshold (more tolerant)
```

## Security Best Practices

✅ **DO**:
- Log security events
- Mask sensitive data
- Include trace IDs
- Use structured logging
- Audit data modifications
- Log authorization failures

❌ **DON'T**:
- Log passwords
- Log full JWT tokens
- Log API keys
- Log credit cards
- Log in plain text
- Mix sensitive data with logs

## Testing Patterns

### Unit Test: Verify Retry
```java
@Test
void testRetryBehavior() {
    // Mock database to fail twice
    when(userRepository.save(...))
        .thenThrow(new IOException())
        .thenThrow(new IOException())
        .thenReturn(user);
    
    // Call should succeed on 3rd attempt
    AuthResponse response = authService.register(request);
    
    // Verify 3 calls made
    verify(userRepository, times(3)).save(...);
}
```

### Integration Test: Circuit Breaker
```java
@Test
void testCircuitBreakerOpens() {
    // Simulate 50+ failure rate
    for (int i = 0; i < 10; i++) {
        when(userRepository.findByEmail(...))
            .thenThrow(new IOException());
    }
    
    // Call multiple times - circuit should open
    for (int i = 0; i < 15; i++) {
        try {
            authService.login(request);
        } catch (RuntimeException e) {
            // Expect "Service temporarily unavailable"
        }
    }
}
```

## Debugging with Trace IDs

### Scenario: User reports error at 10:30 AM
1. Ask for trace ID from error message
2. Search logs: `grep "550e8400..." logs/spring.log`
3. View entire request flow
4. Trace from controller → service → repository
5. Find actual error cause
6. Fix and deploy

## Integration with ELK

### Elasticsearch Configuration
```json
{
  "index_patterns": ["spring.log*"],
  "mappings": {
    "properties": {
      "@timestamp": { "type": "date" },
      "traceId": { "type": "keyword" },
      "eventType": { "type": "keyword" },
      "email": { "type": "keyword" },
      "userId": { "type": "keyword" }
    }
  }
}
```

### Kibana Query Examples
```
# All authentication failures
eventType: "AUTHENTICATION_ATTEMPT" AND success: false

# Slow responses
httpMethod: "POST" AND @duration: > 1000

# Authorization failures
eventType: "AUTHORIZATION_FAILURE"

# Circuit breaker state changes
logger_name: "io.github.resilience4j*"
```

---

**Last Updated**: December 6, 2025  
**Version**: 1.0  
**Compatibility**: Spring Boot 3.3.x, Java 21+
