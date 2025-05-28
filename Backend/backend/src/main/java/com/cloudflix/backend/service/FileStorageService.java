//src/main/java/com/cloudflix/backend/service/FileStorageService.java
package com.cloudflix.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Define the root location for storing videos
    // This should match the one used in VideoController for streaming
    private final Path videoStorageLocation;

    public FileStorageService() {
        // Configure this path. For now, relative to project root.
        // Ensure this directory exists or is created on startup.
        this.videoStorageLocation = Paths.get("uploads/videos").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.videoStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Stores a file and returns its unique generated filename.
     *
     * @param file The multipart file to store.
     * @param desiredTitle The user-provided title, used for generating a readable part of the filename.
     * @return The unique filename under which the file is stored.
     * @throws IOException if an error occurs during file storage.
     */
    public String storeFile(MultipartFile file, String desiredTitle) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        // Sanitize and create a unique filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(lastDot); // .mp4, .mkv etc.
        }

        String sanitizedTitlePart = desiredTitle.replaceAll("\\s+", "_")
                                                .replaceAll("[^a-zA-Z0-9._-]", "");
        if (sanitizedTitlePart.length() > 50) { // Truncate if too long
            sanitizedTitlePart = sanitizedTitlePart.substring(0, 50);
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedTitlePart + fileExtension;

        Path targetLocation = this.videoStorageLocation.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return uniqueFileName; // This is the key to be stored in the Video entity's storageObjectKey
    }

    /**
     * Deletes a file given its filename (storageObjectKey).
     * @param filename The name of the file to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deleteFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.videoStorageLocation.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error: "Failed to delete file " + filename
            System.err.println("Error deleting file: " + filename + " - " + ex.getMessage());
            return false;
        }
    }

    // Optional: Method to load file as resource (can be used by VideoService for streaming if you centralize it here)
    // public Resource loadFileAsResource(String filename) { ... }
}