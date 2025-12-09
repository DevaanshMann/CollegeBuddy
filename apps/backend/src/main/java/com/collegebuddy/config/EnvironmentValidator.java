package com.collegebuddy.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates that required environment variables are set on application startup.
 */
@Configuration
public class EnvironmentValidator {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentValidator.class);

    @Value("${collegebuddy.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void validate() {
        log.info("Validating required environment variables...");

        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable must be set");
        }

        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long for security");
        }

        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalStateException("DB_URL environment variable must be set");
        }

        if (dbUsername == null || dbUsername.trim().isEmpty()) {
            throw new IllegalStateException("DB_USERNAME environment variable must be set");
        }

        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            log.warn("DB_PASSWORD is empty - this is insecure for production!");
        }

        log.info("Environment variables validation completed successfully");
    }
}
