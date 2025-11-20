package com.collegebuddy.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Email delivery strategy using SMTP via Spring's JavaMailSender.
 * Suitable for production use with configured mail server.
 *
 * Activated when: collegebuddy.email.strategy=smtp
 *
 * Requires configuration in application.properties:
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@example.com
 * spring.mail.password=your-password
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 */
@Component
@ConditionalOnProperty(name = "collegebuddy.email.strategy", havingValue = "smtp")
public class SmtpEmailDeliveryStrategy implements EmailDeliveryStrategy {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailDeliveryStrategy.class);

    private final JavaMailSender mailSender;

    @Value("${collegebuddy.email.from:noreply@collegebuddy.app}")
    private String fromAddress;

    public SmtpEmailDeliveryStrategy(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            helper.setText(message.getBody(), message.isHtml());

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", message.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", message.getTo(), e);
            throw new EmailDeliveryException("Failed to send email", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }
}
