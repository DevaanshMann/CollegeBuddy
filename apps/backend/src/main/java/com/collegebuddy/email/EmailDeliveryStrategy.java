package com.collegebuddy.email;

/**
 * Strategy interface for email delivery implementations.
 * Allows switching between different email providers (SMTP, SendGrid, SES, etc.)
 * without changing business logic.
 *
 * Implements Strategy Pattern for pluggable email delivery backends.
 */
public interface EmailDeliveryStrategy {

    /**
     * Sends an email message.
     *
     * @param message The email message to send
     * @throws EmailDeliveryException if sending fails
     */
    void send(EmailMessage message);

    /**
     * Checks if the email delivery service is available/configured.
     *
     * @return true if the service is ready to send emails
     */
    boolean isAvailable();
}
