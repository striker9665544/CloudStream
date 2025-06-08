//src/main/java/com/cloudflix/backend/service/storage/CloudStorageService.java
package com.cloudflix.backend.service.storage; // Adjust package if needed

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface CloudStorageService {

    /**
     * Stores the given multipart file.
     *
     * @param file The file to store.
     * @param desiredBaseName A suggested base name for the file (e.g., sanitized title),
     *                        the implementation will ensure uniqueness and add extension.
     * @return The unique key or filename under which the file was stored.
     *         This key will be used to retrieve or delete the file.
     * @throws IOException If an error occurs during file storage.
     * @throws IllegalArgumentException If the file is empty or invalid.
     */
    String store(MultipartFile file, String desiredBaseName) throws IOException, IllegalArgumentException;

    /**
     * Loads a file as a Spring Resource.
     * This is suitable for streaming the file content.
     *
     * @param storageKey The unique key or filename of the file to load.
     * @return A Resource representing the file.
     * @throws RuntimeException If the file cannot be found or read.
     */
    Resource loadAsResource(String storageKey);

    /**
     * Generates a publicly accessible URL or a pre-signed URL for the stored file.
     * For local storage, this might be a path that a controller can serve.
     * For cloud storage (S3, Azure), this would typically be a pre-signed URL with an expiration.
     *
     * @param storageKey The unique key or filename of the file.
     * @return A URL string.
     * @throws RuntimeException If the URL cannot be generated.
     */
    String getFileUrl(String storageKey); // Or getPresignedUrl if always presigned

    /**
     * Deletes the file associated with the given storage key.
     *
     * @param storageKey The unique key or filename of the file to delete.
     * @return true if the file was successfully deleted or did not exist, false otherwise.
     */
    boolean delete(String storageKey);

    /**
     * Gets the root path where files are stored for this service.
     * Mainly useful for local storage implementation or for constructing full URLs.
     * Might be less relevant or differently interpreted for cloud blob storage.
     *
     * @return The root storage path.
     */
    Path getRootLocation(); // Optional, might be specific to local storage
}