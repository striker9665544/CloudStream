// src/main/java/com/cloudflix/backend/service/storage/AzureBlobStorageServiceImpl.java
package com.cloudflix.backend.service.storage;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.cloudflix.backend.exception.StorageException;
import com.cloudflix.backend.exception.StorageFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path; // For interface compatibility, not directly used for Azure paths
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service("azureBlobStorageService")
@Profile("azure") // This bean will be active when the 'azure' Spring profile is active
public class AzureBlobStorageServiceImpl implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageServiceImpl.class);

    private final String containerName;
    private final BlobContainerClient blobContainerClient;
    private final long sasTokenDurationHours;
    private final String storageAccountUrl;


    public AzureBlobStorageServiceImpl(
            @Value("${azure.storage.account-name}") String accountName,
            @Value("${azure.storage.container-name}") String containerName,
            @Value("${azure.storage.connection-string:#{null}}") String connectionString, // Optional, for fallback
            @Value("${azure.storage.sas-token.duration-hours:1}") long sasTokenDurationHours) {
    	
    	logger.info("AzureBlobStorageServiceImpl CONSTRUCTOR - Received connectionString: '{}'", connectionString);

        if (containerName == null || containerName.trim().isEmpty()) {
            throw new StorageException("Azure Blob container name cannot be empty.");
        }
        this.containerName = containerName;
        this.sasTokenDurationHours = sasTokenDurationHours;
        this.storageAccountUrl = String.format("https://%s.blob.core.windows.net", accountName);

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();

        if (connectionString != null && !connectionString.isEmpty()) {
            logger.info("Authenticating to Azure Blob Storage using Connection String.");
            builder.connectionString(connectionString);
        } else {
            logger.info("Authenticating to Azure Blob Storage using DefaultAzureCredential (e.g., az login, env vars, managed identity).");
            builder.endpoint(this.storageAccountUrl)
                   .credential(new DefaultAzureCredentialBuilder().build());
        }
        
        BlobServiceClient blobServiceClient = builder.buildClient();
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(this.containerName);

        if (!this.blobContainerClient.exists()) {
            logger.info("Blob container {} does not exist, creating it.", this.containerName);
            this.blobContainerClient.create();
        }
        logger.info("AzureBlobStorageService initialized for container: {} in account: {}", this.containerName, accountName);
    }


    @Override
    public String store(MultipartFile file, String desiredBaseName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty or null file.");
        }
        if (desiredBaseName == null || desiredBaseName.trim().isEmpty()) {
            desiredBaseName = "video";
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(lastDot).toLowerCase();
        }

        String sanitizedTitlePart = desiredBaseName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
        if (sanitizedTitlePart.length() > 100) {
            sanitizedTitlePart = sanitizedTitlePart.substring(0, 100);
        }
        if (sanitizedTitlePart.isEmpty()) {
            sanitizedTitlePart = "file";
        }

        // Azure blob names can include "paths" like S3 object keys
        String blobName = "videos/" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedTitlePart + fileExtension;

        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true); // true to overwrite if exists

            // Set Content-Type metadata on the blob
            String contentType = file.getContentType();
            if (contentType == null || contentType.equals("application/octet-stream") || contentType.isEmpty()) {
                if (blobName.toLowerCase().endsWith(".mp4")) contentType = "video/mp4";
                else if (blobName.toLowerCase().endsWith(".webm")) contentType = "video/webm";
                else contentType = "application/octet-stream";
            }
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.setHttpHeaders(headers);

            logger.info("Successfully uploaded {} to Azure Blob container {} as blob {}", originalFileName, containerName, blobName);
            return blobName; // Return the blob name (which acts as the storageKey)
        } catch (Exception e) { // Catch Azure SDK specific exceptions too if needed
            logger.error("Error uploading file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new StorageException("Failed to store file " + originalFileName + " to Azure Blob. " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        // Similar to S3, getFileUrl (for SAS URL) is preferred for player.
        // This method could download the blob to a temp file and return a FileSystemResource,
        // or return a custom Resource wrapper for the blob stream.
        // For now, returning a UrlResource based on a SAS URL.
        try {
            URL sasUrl = new URL(getFileUrl(storageKey));
            return new UrlResource(sasUrl);
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not create URL resource for Azure blob: " + storageKey, e);
        }
    }

    @Override
    public String getFileUrl(String storageKey) { // storageKey is the full blob name e.g., "videos/yourfile.mp4"
        if (storageKey == null || storageKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage key (blob name) cannot be null or empty for generating SAS URL.");
        }
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(storageKey);

            if (!blobClient.exists()) {
                logger.warn("Attempting to generate SAS URL for non-existent blob: {} in container {}", storageKey, this.containerName);
                throw new StorageFileNotFoundException("Blob not found: " + storageKey);
            }

            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plusHours(this.sasTokenDurationHours),
                    new BlobSasPermission().setReadPermission(true)
            );

            String sasToken = blobClient.generateSas(sasValues);
            // Use getBlobUrl() which correctly includes the account, container, and blob name
            String urlWithSas = blobClient.getBlobUrl() + "?" + sasToken;

            logger.debug("Generated SAS URL for blob {} in container {}: {}", storageKey, this.containerName, urlWithSas);
            return urlWithSas;

        } catch (Exception e) {
            logger.error("Error generating SAS URL for blob {} in container {}: {}", storageKey, this.containerName, e.getMessage(), e);
            throw new StorageException("Could not generate SAS URL for blob: " + storageKey, e);
        }
    }

    @Override
    public boolean delete(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            return false;
        }
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(storageKey);
            blobClient.deleteIfExistsWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, null, null);
            logger.info("Successfully deleted blob {} from Azure container {}", storageKey, containerName);
            return true;
        } catch (Exception e) { // Catch Azure SDK specific exceptions
            logger.error("Error deleting blob {} from Azure: {}", storageKey, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Path getRootLocation() {
        // Not directly applicable to Azure Blob Storage in the same way as local file system.
        return null;
    }
}