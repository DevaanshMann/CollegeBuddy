package com.collegebuddy.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Email delivery strategy that logs emails instead of sending them.
 * Useful for development and testing environments.
 *
 * Activated when: collegebuddy.email.strategy=logging (or not set)
 */
@Component
@ConditionalOnProperty(
        name = "collegebuddy.email.strategy",
        havingValue = "logging",
        matchIfMissing = true  // Default to logging if not configured
)
public class LoggingEmailDeliveryStrategy implements EmailDeliveryStrategy {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailDeliveryStrategy.class);

    @Override
    public void send(EmailMessage message) {
        log.info("=== EMAIL (NOT ACTUALLY SENT) ===");
        log.info("To: {}", message.getTo());
        log.info("Subject: {}", message.getSubject());
        log.info("Body: {}", message.getBody());
        log.info("HTML: {}", message.isHtml());
        log.info("==================================");
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available
    }
}
