package com.collegebuddy.media;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles media upload/storage and returns URLs.
 * Uses Strategy Pattern to delegate to pluggable storage backends.
 *
 * The actual storage implementation (local, S3, etc.) can be switched
 * by providing a different MediaStorageStrategy bean.
 */
@Service
public class MediaStorageService {

    private final MediaStorageStrategy storageStrategy;

    public MediaStorageService(MediaStorageStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @PostConstruct
    public void initialize() {
        storageStrategy.initialize();
    }

    /**
     * Stores an avatar image and returns its URL.
     *
     * @param file The image file to store
     * @param userId The ID of the user uploading the avatar
     * @return The URL where the avatar can be accessed
     * @throws IllegalArgumentException if file is invalid
     * @throws StorageException if storage fails
     */
    public String storeAvatar(MultipartFile file, Long userId) {
        return storageStrategy.store(file, userId, StorageContext.avatar());
    }

    /**
     * Deletes a file from storage.
     *
     * @param fileUrl The URL of the file to delete
     * @return true if deleted, false if file didn't exist
     */
    public boolean deleteFile(String fileUrl) {
        return storageStrategy.delete(fileUrl);
    }
}
