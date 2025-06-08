// src/main/java/com/cloudflix/backend/service/storage/LocalStorageServiceImpl.java
package com.cloudflix.backend.service.storage; // Assuming this package

import com.cloudflix.backend.exception.StorageException; // Custom exception for storage issues
import com.cloudflix.backend.exception.StorageFileNotFoundException; // Custom exception for file not found
import org.springframework.beans.factory.annotation.Value; // For injecting configuration
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Profile; // <<< Ensure this is imported
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service("localStorageService") // Give it a qualifier name if you plan to have multiple impls
@Profile({"local", "default"}) // Active if 'local' or 'default' profile is active, or if no profile is active
public class LocalStorageServiceImpl implements CloudStorageService {

    private final Path rootLocation;
    private final String baseUrlForFiles; // For constructing URLs

    // Constructor to initialize the storage location
    // You can inject this path from application.properties if you want it to be configurable
    public LocalStorageServiceImpl(@Value("${storage.local.root-path:uploads/videos}") String rootPath,
                                   @Value("${storage.local.base-url:/media-files}") String baseUrl) { // Base URL for accessing files via HTTP
        if (rootPath == null || rootPath.trim().isEmpty()) {
            throw new StorageException("Local storage root path cannot be empty.");
        }
        this.rootLocation = Paths.get(rootPath).toAbsolutePath().normalize();
        this.baseUrlForFiles = baseUrl; // e.g. /api/files or /media-files

        try {
            Files.createDirectories(this.rootLocation);
            System.out.println("Initialized local storage at: " + this.rootLocation.toString());
        } catch (Exception ex) {
            throw new StorageException("Could not create the directory where the uploaded files will be stored: " + this.rootLocation.toString(), ex);
        }
    }

    @Override
    public String store(MultipartFile file, String desiredBaseName) throws IOException, IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty or null file.");
        }
        if (desiredBaseName == null || desiredBaseName.trim().isEmpty()) {
            desiredBaseName = "video"; // Default base name if none provided
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(lastDot).toLowerCase();
        }

        String sanitizedTitlePart = desiredBaseName.replaceAll("\\s+", "_")
                                                .replaceAll("[^a-zA-Z0-9._-]", "");
        if (sanitizedTitlePart.length() > 100) { // Limit sanitized part length
            sanitizedTitlePart = sanitizedTitlePart.substring(0, 100);
        }
        if (sanitizedTitlePart.isEmpty()) {
            sanitizedTitlePart = "file";
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedTitlePart + fileExtension;
        Path targetLocation = this.rootLocation.resolve(uniqueFileName).normalize();

        // Ensure the target location is within the root storage location (security check)
        if (!targetLocation.getParent().equals(this.rootLocation)) {
            throw new StorageException("Cannot store file outside current directory structure: " + originalFileName);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Stored file: " + uniqueFileName + " at: " + targetLocation.toString());
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + uniqueFileName, e);
        }

        return uniqueFileName;
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            throw new StorageFileNotFoundException("Storage key cannot be empty.");
        }
        try {
            Path file = this.rootLocation.resolve(storageKey).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                System.err.println("Could not read file (loadAsResource): " + file.toString());
                throw new StorageFileNotFoundException("Could not read file: " + storageKey);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file (Malformed URL): " + storageKey, e);
        }
    }

    @Override
    public String getFileUrl(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            return null; // Or throw exception
        }
        // For local storage, we construct a URL that a dedicated controller endpoint will serve
        // This assumes you have a controller mapped to this.baseUrlForFiles
        return this.baseUrlForFiles + "/" + storageKey;
    }

    @Override
    public boolean delete(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.rootLocation.resolve(storageKey).normalize();
            // Security check: ensure file is within root location
            if (!filePath.startsWith(this.rootLocation)) {
                 System.err.println("Attempt to delete file outside storage root: " + filePath);
                return false;
            }
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            System.err.println("Error deleting file from local storage: " + storageKey + " - " + ex.getMessage());
            // Depending on policy, you might want to throw StorageException here
            return false;
        }
    }

    @Override
    public Path getRootLocation() {
        return this.rootLocation;
    }

    // Optional: Method to delete all files (useful for cleanup in dev/test)
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    // Optional: Method to initialize storage (called from constructor or a @PostConstruct method)
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}