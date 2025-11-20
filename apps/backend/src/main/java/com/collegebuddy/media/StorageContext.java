package com.collegebuddy.media;

import java.util.Set;

/**
 * Context object for storage operations containing validation rules and metadata.
 */
public class StorageContext {

    private final Set<String> allowedContentTypes;
    private final Long maxSizeBytes;
    private final String category; // e.g., "avatar", "document", "image"

    private StorageContext(Builder builder) {
        this.allowedContentTypes = builder.allowedContentTypes;
        this.maxSizeBytes = builder.maxSizeBytes;
        this.category = builder.category;
    }

    public Set<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public Long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public String getCategory() {
        return category;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<String> allowedContentTypes;
        private Long maxSizeBytes;
        private String category;

        public Builder allowedContentTypes(Set<String> allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes;
            return this;
        }

        public Builder maxSizeBytes(Long maxSizeBytes) {
            this.maxSizeBytes = maxSizeBytes;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public StorageContext build() {
            return new StorageContext(this);
        }
    }

    /**
     * Predefined context for avatar uploads.
     */
    public static StorageContext avatar() {
        return builder()
                .allowedContentTypes(Set.of("image/jpeg", "image/png", "image/gif", "image/webp"))
                .maxSizeBytes(5L * 1024 * 1024) // 5MB
                .category("avatar")
                .build();
    }
}
