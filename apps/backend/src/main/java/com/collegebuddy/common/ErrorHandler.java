package com.collegebuddy.common;

import com.collegebuddy.common.exceptions.*;
import com.collegebuddy.email.EmailDeliveryException;
import com.collegebuddy.media.StorageException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Global exception handler so controllers can just throw.
 * Now uses ErrorResponseFactory for consistent, structured error responses.
 */
@RestControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private final ErrorResponseFactory errorResponseFactory;

    public ErrorHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailDomain(
            InvalidEmailDomainException ex, HttpServletRequest request) {
        log.warn("Invalid email domain attempt: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenCampusAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenCampus(
            ForbiddenCampusAccessException ex, HttpServletRequest request) {
        log.warn("Forbidden campus access attempt: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyInUse(
            EmailAlreadyInUseException ex, HttpServletRequest request) {
        log.info("Email already in use: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationToken(
            InvalidVerificationTokenException ex, HttpServletRequest request) {
        log.warn("Invalid verification token: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProfileVisibilityException.class)
    public ResponseEntity<ErrorResponse> handleProfileVisibility(
            ProfileVisibilityException ex, HttpServletRequest request) {
        log.warn("Profile visibility restriction: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AlreadyConnectedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyConnected(
            AlreadyConnectedException ex, HttpServletRequest request) {
        log.info("Already connected attempt: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ConnectionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConnectionNotFound(
            ConnectionNotFoundException ex, HttpServletRequest request) {
        log.info("Connection not found: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConnectionRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConnectionRequestNotFound(
            ConnectionRequestNotFoundException ex, HttpServletRequest request) {
        log.info("Connection request not found: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConnectionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConnectionAlreadyExists(
            ConnectionAlreadyExistsException ex, HttpServletRequest request) {
        log.info("Connection already exists: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidConnectionActionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidConnectionAction(
            InvalidConnectionActionException ex, HttpServletRequest request) {
        log.warn("Invalid connection action: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MessagingNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleMessagingNotAllowed(
            MessagingNotAllowedException ex, HttpServletRequest request) {
        log.warn("Messaging not allowed: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MessagePermissionException.class)
    public ResponseEntity<ErrorResponse> handleMessagePermission(
            MessagePermissionException ex, HttpServletRequest request) {
        log.warn("Message permission denied: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex, HttpServletRequest request) {
        log.error("Storage operation failed: {}", ex.getMessage(), ex);
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleEmailDeliveryException(
            EmailDeliveryException ex, HttpServletRequest request) {
        log.error("Email delivery failed: {}", ex.getMessage(), ex);
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = errorResponseFactory.createErrorResponse(ex, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
