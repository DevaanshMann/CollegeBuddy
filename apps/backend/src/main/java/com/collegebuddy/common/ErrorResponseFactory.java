package com.collegebuddy.common;

import com.collegebuddy.common.exceptions.AlreadyConnectedException;
import com.collegebuddy.common.exceptions.ConnectionAlreadyExistsException;
import com.collegebuddy.common.exceptions.ConnectionNotFoundException;
import com.collegebuddy.common.exceptions.ConnectionRequestNotFoundException;
import com.collegebuddy.common.exceptions.EmailAlreadyInUseException;
import com.collegebuddy.common.exceptions.ForbiddenCampusAccessException;
import com.collegebuddy.common.exceptions.InvalidConnectionActionException;
import com.collegebuddy.common.exceptions.InvalidEmailDomainException;
import com.collegebuddy.common.exceptions.InvalidVerificationTokenException;
import com.collegebuddy.common.exceptions.MessagePermissionException;
import com.collegebuddy.common.exceptions.MessagingNotAllowedException;
import com.collegebuddy.common.exceptions.ProfileVisibilityException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import com.collegebuddy.email.EmailDeliveryException;
import com.collegebuddy.media.StorageException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorResponseFactory {
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

    private ErrorResponse createValidationError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "VALIDATION_ERROR"))
                .build();
    }

    private ErrorResponse createAuthError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "AUTHENTICATION_ERROR"))
                .build();
    }

    private ErrorResponse createConflictError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "CONFLICT_ERROR"))
                .build();
    }

    private ErrorResponse createNotFoundError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "NOT_FOUND_ERROR"))
                .build();
    }

    private ErrorResponse createInternalError(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(createDetails("category", "INTERNAL_ERROR"))
                .build();
    }

    private Map<String, Object> createDetails(String key, Object value) {
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        return details;
    }
}
