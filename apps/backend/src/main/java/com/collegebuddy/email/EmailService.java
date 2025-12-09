package com.collegebuddy.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email service that sends emails using pluggable delivery strategies.
 * Uses Strategy Pattern to switch between different email providers.
 *
 * The actual delivery mechanism (logging, SMTP, SendGrid, etc.) is determined
 * by the configured EmailDeliveryStrategy bean.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailDeliveryStrategy deliveryStrategy;

    @Value("${collegebuddy.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(EmailDeliveryStrategy deliveryStrategy) {
        this.deliveryStrategy = deliveryStrategy;
    }

    /**
     * Sends a verification email with a token link.
     *
     * @param toAddress The recipient email address
     * @param tokenValue The verification token
     */
    public void sendVerificationEmail(String toAddress, String tokenValue) {
        String verificationLink = frontendUrl + "/verify?token=" + tokenValue;

        EmailMessage message = EmailMessage.builder()
                .to(toAddress)
                .subject("Verify your CollegeBuddy account")
                .body(buildVerificationEmailBody(verificationLink))
                .html(true)
                .build();

        try {
            deliveryStrategy.send(message);
            log.info("Verification email sent to: {}", toAddress);
        } catch (EmailDeliveryException e) {
            log.error("Failed to send verification email to: {}", toAddress, e);
            throw e;
        }
    }

    /**
     * Sends a password reset email with a token link.
     *
     * @param toAddress The recipient email address
     * @param tokenValue The password reset token
     */
    public void sendPasswordResetEmail(String toAddress, String tokenValue) {
        String resetLink = frontendUrl + "/reset-password?token=" + tokenValue;

        EmailMessage message = EmailMessage.builder()
                .to(toAddress)
                .subject("Reset your CollegeBuddy password")
                .body(buildPasswordResetEmailBody(resetLink))
                .html(true)
                .build();

        try {
            deliveryStrategy.send(message);
            log.info("Password reset email sent to: {}", toAddress);
        } catch (EmailDeliveryException e) {
            log.error("Failed to send password reset email to: {}", toAddress, e);
            throw e;
        }
    }

    /**
     * Sends a notification email.
     *
     * @param toAddress The recipient email address
     * @param body The email body content
     */
    public void sendNotificationEmail(String toAddress, String body) {
        EmailMessage message = EmailMessage.builder()
                .to(toAddress)
                .subject("CollegeBuddy Notification")
                .body(body)
                .html(false)
                .build();

        try {
            deliveryStrategy.send(message);
            log.info("Notification email sent to: {}", toAddress);
        } catch (EmailDeliveryException e) {
            log.error("Failed to send notification email to: {}", toAddress, e);
            throw e;
        }
    }

    /**
     * Builds HTML body for verification email.
     */
    private String buildVerificationEmailBody(String verificationLink) {
        return """
                <html>
                <body>
                    <h2>Welcome to CollegeBuddy!</h2>
                    <p>Please verify your email address by clicking the link below:</p>
                    <p><a href="%s">Verify Email</a></p>
                    <p>If you didn't create this account, you can safely ignore this email.</p>
                    <p>This link will expire in 24 hours.</p>
                </body>
                </html>
                """.formatted(verificationLink);
    }

    /**
     * Builds HTML body for password reset email.
     */
    private String buildPasswordResetEmailBody(String resetLink) {
        return """
                <html>
                <body>
                    <h2>Reset Your Password</h2>
                    <p>We received a request to reset your CollegeBuddy password.</p>
                    <p>Click the link below to reset your password:</p>
                    <p><a href="%s">Reset Password</a></p>
                    <p>If you didn't request a password reset, you can safely ignore this email.</p>
                    <p>This link will expire in 15 minutes.</p>
                </body>
                </html>
                """.formatted(resetLink);
    }
}

