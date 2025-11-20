package com.collegebuddy.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local filesystem implementation of MediaStorageStrategy.
 * Stores files in a local directory for development or single-server deployments.
 *
 * For production with multiple servers, consider using S3StorageStrategy or similar.
 */
@Component
public class LocalFileStorageStrategy implements MediaStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageStrategy.class);

    @Value("${collegebuddy.storage.local.upload-dir:uploads/avatars/}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, Long userId, StorageContext context) {
        // Validate file using default validation from interface
        validateFile(file, context);

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = extractExtension(originalFilename);
            String filename = userId + "_" + UUID.randomUUID() + extension;

            Path targetPath = Paths.get(uploadDir + filename);

            // Save file to local filesystem
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL path
            String url = "/uploads/avatars/" + filename;
            log.info("File stored successfully: {} for user {}", url, userId);

            return url;

        } catch (IOException e) {
            log.error("Failed to store file for user {}", userId, e);
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/uploads/")) {
            return false;
        }

        try {
            // Extract filename from URL
            String filename = extractFilenameFromUrl(fileUrl);
            Path filePath = Paths.get(uploadDir + filename);

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted successfully: {}", fileUrl);
            }
            return deleted;

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            throw new StorageException("Failed to delete file", e);
        }
    }

    @Override
    public void initialize() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            log.info("Local storage initialized at: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize local storage directory", e);
            throw new StorageException("Could not create upload directory", e);
        }
    }

    /**
     * Extracts file extension from original filename.
     */
    private String extractExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return ".jpg"; // default extension
    }

    /**
     * Extracts just the filename from a full URL or path.
     * Handles query parameters and different URL formats.
     */
    private String extractFilenameFromUrl(String fileUrl) {
        // Extract the part after the last "/"
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        // Remove query parameters if present
        if (filename.contains("?")) {
            filename = filename.substring(0, filename.indexOf("?"));
        }

        return filename;
    }
}
