package com.collegebuddy.globalexc;

import com.collegebuddy.exception.CredentialsInvalidException;
import com.collegebuddy.exception.DuplicateEmailException;
import com.collegebuddy.exception.InvalidEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // minimal error body (kept local to avoid extra deps)
    public record ApiError(String code, String message) {}

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ApiError> handleInvalidEmail(InvalidEmailException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("INVALID_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("DUPLICATE_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(CredentialsInvalidException.class)
    public ResponseEntity<ApiError> handleCreds(CredentialsInvalidException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("INVALID_CREDENTIALS", ex.getMessage()));
    }
}
