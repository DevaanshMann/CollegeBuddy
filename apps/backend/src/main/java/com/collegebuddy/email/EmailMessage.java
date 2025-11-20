package com.collegebuddy.email;

/**
 * Represents an email message to be sent.
 * Immutable value object for email data.
 */
public class EmailMessage {

    private final String to;
    private final String subject;
    private final String body;
    private final boolean isHtml;

    private EmailMessage(Builder builder) {
        this.to = builder.to;
        this.subject = builder.subject;
        this.body = builder.body;
        this.isHtml = builder.isHtml;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String to;
        private String subject;
        private String body;
        private boolean isHtml = false;

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder html(boolean isHtml) {
            this.isHtml = isHtml;
            return this;
        }

        public EmailMessage build() {
            if (to == null || subject == null || body == null) {
                throw new IllegalStateException("to, subject, and body are required");
            }
            return new EmailMessage(this);
        }
    }
}
