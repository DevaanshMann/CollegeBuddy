package com.collegebuddy.media;

import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy interface for media storage implementations.
 * Allows switching between different storage backends (local, S3, CDN, etc.)
 * without changing business logic.
 *
 * Implements Strategy Pattern for pluggable storage backends.
 */
public interface MediaStorageStrategy {

    /**
     * Stores a file and returns its accessible URL/path.
     *
     * @param file The file to store
     * @param userId The ID of the user uploading the file
     * @param context Additional context for storage (e.g., file type, category)
     * @return The URL or path where the file can be accessed
     * @throws IllegalArgumentException if file is invalid
     * @throws StorageException if storage operation fails
     */
    String store(MultipartFile file, Long userId, StorageContext context);

    /**
     * Deletes a file from storage.
     *
     * @param fileUrl The URL or identifier of the file to delete
     * @return true if file was deleted, false if file didn't exist
     * @throws StorageException if deletion fails
     */
    boolean delete(String fileUrl);

    /**
     * Initializes the storage backend (e.g., create directories, connect to service).
     * Called once during application startup.
     *
     * @throws StorageException if initialization fails
     */
    void initialize();

    /**
     * Validates a file before storage.
     *
     * @param file The file to validate
     * @param context The storage context with validation rules
     * @throws IllegalArgumentException if file is invalid
     */
    default void validateFile(MultipartFile file, StorageContext context) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (context.getAllowedContentTypes() != null &&
                (contentType == null || !context.getAllowedContentTypes().contains(contentType))) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " +
                    context.getAllowedContentTypes());
        }

        // Validate file size
        if (context.getMaxSizeBytes() != null && file.getSize() > context.getMaxSizeBytes()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " +
                    context.getMaxSizeBytes() / (1024 * 1024) + "MB");
        }
    }
}
