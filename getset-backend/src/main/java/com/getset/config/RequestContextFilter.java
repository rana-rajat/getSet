package com.getset.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add trace ID and request context to MDC (Mapped Diagnostic Context)
 * for structured logging and distributed tracing
 */
@Slf4j
@Component
public class RequestContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String TRACE_ID_MDC = "traceId";
    private static final String REQUEST_ID_MDC = "requestId";
    private static final String USER_ID_MDC = "userId";
    private static final String HTTP_METHOD_MDC = "httpMethod";
    private static final String HTTP_PATH_MDC = "httpPath";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        
        String requestId = UUID.randomUUID().toString();
        
        try {
            // Add context to MDC
            MDC.put(TRACE_ID_MDC, traceId);
            MDC.put(REQUEST_ID_MDC, requestId);
            MDC.put(HTTP_METHOD_MDC, request.getMethod());
            MDC.put(HTTP_PATH_MDC, request.getRequestURI());
            
            // Log incoming request
            log.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
            
            // Add trace ID to response header
            response.setHeader(TRACE_ID_HEADER, traceId);
            
            filterChain.doFilter(request, response);
            
            // Log response
            log.info("Request completed: {} {} - Status: {}", 
                    request.getMethod(), request.getRequestURI(), response.getStatus());
            
        } finally {
            // Clean up MDC
            MDC.remove(TRACE_ID_MDC);
            MDC.remove(REQUEST_ID_MDC);
            MDC.remove(USER_ID_MDC);
            MDC.remove(HTTP_METHOD_MDC);
            MDC.remove(HTTP_PATH_MDC);
        }
    }
}
