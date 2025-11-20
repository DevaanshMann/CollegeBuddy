package com.collegebuddy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response structure for all API errors.
 * Provides consistent format for clients to handle errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final Instant timestamp;
    private final String path;
    private final Map<String, Object> details;

    private ErrorResponse(Builder builder) {
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.timestamp = builder.timestamp;
        this.path = builder.path;
        this.details = builder.details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Builder for creating ErrorResponse instances.
     * Uses builder pattern for flexible, readable construction.
     */
    public static class Builder {
        private String errorCode;
        private String message;
        private Instant timestamp = Instant.now();
        private String path;
        private Map<String, Object> details;

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public ErrorResponse build() {
            if (errorCode == null || message == null) {
                throw new IllegalStateException("errorCode and message are required");
            }
            return new ErrorResponse(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
