package com.collegebuddy.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * For now, just logs the outgoing verification "email".
 * Later you wire this into real SMTP / SES / etc.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(String toAddress, String tokenValue) {
        // In prod you'd build a clickable link like:
        // https://your-frontend/verify?token=...
        log.info("SEND VERIFICATION EMAIL to={} token={}", toAddress, tokenValue);
    }

    public void sendNotificationEmail(String toAddress, String body) {
        log.info("SEND NOTIFICATION EMAIL to={} body={}", toAddress, body);
    }
}
