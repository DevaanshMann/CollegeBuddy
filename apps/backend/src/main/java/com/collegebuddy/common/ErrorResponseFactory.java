package com.collegebuddy.common;

import com.collegebuddy.common.exceptions.*;
import com.collegebuddy.email.EmailDeliveryException;
import com.collegebuddy.media.StorageException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating standardized ErrorResponse objects from exceptions.
 * Centralizes error response creation logic and ensures consistency.
 *
 * Implements Factory Pattern to encapsulate error response creation.
 */
@Component
public class ErrorResponseFactory {

    /**
     * Creates an ErrorResponse from any exception with appropriate error code and context.
     *
     * @param exception The exception to convert
     * @param path The request path where the error occurred
     * @return Structured ErrorResponse object
     */
    public ErrorResponse createErrorResponse(Exception exception, String path) {
        if (exception instanceof InvalidEmailDomainException) {
            return createValidationError("INVALID_EMAIL_DOMAIN", exception.getMessage(), path);
        } else if (exception instanceof InvalidVerificationTokenException) {
            return createValidationError("INVALID_VERIFICATION_TOKEN", exception.getMessage(), path);
        } else if (exception instanceof EmailAlreadyInUseException) {
            return createConflictError("EMAIL_ALREADY_IN_USE", exception.getMessage(), path);
        } else if (exception instanceof UnauthorizedException) {
            return createAuthError("UNAUTHORIZED", exception.getMessage(), path);
        } else if (exception instanceof ForbiddenCampusAccessException) {
            return createAuthError("FORBIDDEN_CAMPUS_ACCESS", exception.getMessage(), path);
        } else if (exception instanceof ProfileVisibilityException) {
            return createAuthError("PROFILE_VISIBILITY_RESTRICTED", exception.getMessage(), path);
        } else if (exception instanceof AlreadyConnectedException) {
            return createConflictError("ALREADY_CONNECTED", exception.getMessage(), path);
        } else if (exception instanceof ConnectionNotFoundException) {
            return createNotFoundError("CONNECTION_NOT_FOUND", exception.getMessage(), path);
        } else if (exception instanceof ConnectionRequestNotFoundException) {
            return createNotFoundError("CONNECTION_REQUEST_NOT_FOUND", exception.getMessage(), path);
        } else if (exception instanceof ConnectionAlreadyExistsException) {
            return createConflictError("CONNECTION_ALREADY_EXISTS", exception.getMessage(), path);
        } else if (exception instanceof InvalidConnectionActionException) {
            return createValidationError("INVALID_CONNECTION_ACTION", exception.getMessage(), path);
        } else if (exception instanceof MessagingNotAllowedException) {
            return createAuthError("MESSAGING_NOT_ALLOWED", exception.getMessage(), path);
        } else if (exception instanceof MessagePermissionException) {
            return createAuthError("MESSAGE_PERMISSION_DENIED", exception.getMessage(), path);
        } else if (exception instanceof IllegalArgumentException) {
            return createValidationError("INVALID_ARGUMENT", exception.getMessage(), path);
        } else if (exception instanceof StorageException) {
            return createInternalError("STORAGE_ERROR", exception.getMessage(), path);
        } else if (exception instanceof EmailDeliveryException) {
            return createInternalError("EMAIL_DELIVERY_ERROR", exception.getMessage(), path);
        } else {
            return createInternalError("INTERNAL_SERVER_ERROR", "An unexpected error occurred", path);
        }
    }

    /**
     * Creates a validation error response (400 Bad Request category).
     */
    private ErrorResponse createValidationError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "VALIDATION_ERROR"))
                .build();
    }

    /**
     * Creates an authentication/authorization error response (401/403 category).
     */
    private ErrorResponse createAuthError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "AUTHENTICATION_ERROR"))
                .build();
    }

    /**
     * Creates a conflict error response (409 Conflict category).
     */
    private ErrorResponse createConflictError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "CONFLICT_ERROR"))
                .build();
    }

    /**
     * Creates a not found error response (404 Not Found category).
     */
    private ErrorResponse createNotFoundError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "NOT_FOUND_ERROR"))
                .build();
    }

    /**
     * Creates an internal server error response (500 category).
     */
    private ErrorResponse createInternalError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "INTERNAL_ERROR"))
                .build();
    }

    /**
     * Helper method to create details map.
     */
    private Map<String, Object> createDetails(String key, Object value) {
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        return details;
    }
}
