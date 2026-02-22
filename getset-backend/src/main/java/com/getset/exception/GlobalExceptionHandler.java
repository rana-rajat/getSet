package com.getset.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.getset.common.ApiError;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 * Centralizes exception handling across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(GetSetException.class)
        public ResponseEntity<ApiError> handleGetSetException(
                        GetSetException ex,
                        WebRequest request) {

                HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
                ApiError error = ApiError.builder()
                                .status(status.value())
                                .message(ex.getMessage())
                                .error(ex.getErrorCode())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, status);
        }

        @ExceptionHandler(com.getset.common.NotFoundException.class)
        public ResponseEntity<ApiError> handleNotFoundException(
                        com.getset.common.NotFoundException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .error("NOT_FOUND")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ApiError> handleForbiddenException(
                        ForbiddenException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message(ex.getMessage())
                                .error("FORBIDDEN")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiError> handleUnauthorizedException(
                        UnauthorizedException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message(ex.getMessage())
                                .error("UNAUTHORIZED")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiError> handleValidationException(
                        ValidationException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .error("VALIDATION_ERROR")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDeniedException(
                        AccessDeniedException ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message("Access denied")
                                .error("ACCESS_DENIED")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationExceptions(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                Map<String, String> details = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));

                ApiError error = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Validation failed")
                                .error("VALIDATION_ERROR")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(details)
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGenericException(
                        Exception ex,
                        WebRequest request) {

                ApiError error = ApiError.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("An unexpected error occurred")
                                .error("INTERNAL_SERVER_ERROR")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .timestamp(Instant.now())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /**
         * Maps error code to appropriate HTTP status.
         */
        private HttpStatus mapErrorCodeToHttpStatus(String errorCode) {
                return switch (errorCode) {
                        case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
                        case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
                        case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
                        case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
                        default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };
        }
}
