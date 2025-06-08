// src/main/java/com/cloudflix/backend/service/storage/S3StorageServiceImpl.java
package com.cloudflix.backend.service.storage;

import com.cloudflix.backend.exception.StorageException;
import com.cloudflix.backend.exception.StorageFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource; // Used for pre-signed URLs
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path; // Not directly used for S3 paths, but for interface
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service("s3StorageService")
@Profile("aws") // This bean will be active when the 'aws' Spring profile is active
public class S3StorageServiceImpl implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final String bucketName;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final long presignedUrlDurationMinutes;

    public S3StorageServiceImpl(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.presigned-url.duration-minutes:15}") long presignedUrlDurationMinutes) {

        if (bucketName == null || bucketName.trim().isEmpty()) {
            throw new StorageException("AWS S3 bucket name cannot be empty.");
        }
        if (region == null || region.trim().isEmpty()) {
            throw new StorageException("AWS S3 region cannot be empty.");
        }

        this.bucketName = bucketName;
        this.presignedUrlDurationMinutes = presignedUrlDurationMinutes;

        // SDK will automatically attempt to find credentials from the chain:
        // 1. Java system properties (aws.accessKeyId, aws.secretKey)
        // 2. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
        // 3. Web Identity Token credentials
        // 4. Shared credentials and config files (~/.aws/credentials, ~/.aws/config)
        // 5. EC2 Instance Profile / ECS Task Role / EKS IAM Role for Service Account
        this.s3Client = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create()) // Uses default credential chain
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        logger.info("S3StorageService initialized for bucket: {} in region: {}", bucketName, region);
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
        
        // S3 object keys often include "paths" for organization, e.g., "videos/"
        String objectKey = "videos/" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitizedTitlePart + fileExtension;


        // Determine Content-Type from the uploaded file
        String contentType = file.getContentType(); // Get Content-Type from MultipartFile
        if (contentType == null || contentType.equals("application/octet-stream")) {
            // Fallback if browser didn't provide a good one, or improve detection
            if (objectKey.toLowerCase().endsWith(".mp4")) contentType = "video/mp4";
            else if (objectKey.toLowerCase().endsWith(".webm")) contentType = "video/webm";
            else if (objectKey.toLowerCase().endsWith(".ogg")) contentType = "video/ogg";
            // Add more or use a library for better MIME type detection if needed
            else contentType = "application/octet-stream"; // Default if still unknown
        }
        logger.info("Determined Content-Type for S3 upload: {}", contentType);
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    // Optional: .contentDisposition("inline") if you want to strongly suggest inline playback
                    //.contentType(file.getContentType()) // Optional: S3 can often infer or set it
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            logger.info("Successfully uploaded {} to S3 bucket {} with key {} and Content-Type {}", originalFileName, bucketName, objectKey, contentType);
            return objectKey; // Return the S3 object key
        } catch (SdkException e) { // Catch AWS SDK specific exceptions
            logger.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new StorageException("Failed to store file " + originalFileName + " to S3. " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        // For direct streaming through the backend (less efficient than pre-signed URLs for S3):
        // You would need to get an InputStream from S3 and wrap it in an InputStreamResource.
        // GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        //         .bucket(bucketName)
        //         .key(storageKey)
        //         .build();
        // ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
        // return new InputStreamResource(s3ObjectStream);
        // Note: This stream needs to be closed properly by the caller or Spring's resource handling.
        // And byte-range handling for this approach through backend would be complex.

        // Given the typical S3 pattern, getFileUrl (for pre-signed URL) is preferred for player.
        // This loadAsResource might be used if the backend needs to process the file itself.
        // For now, returning a UrlResource based on a pre-signed URL to show it *can* be loaded.
        try {
            URL presignedUrl = new URL(getFileUrl(storageKey)); // Generate a short-lived presigned URL
            return new UrlResource(presignedUrl);
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not create URL resource for S3 object: " + storageKey, e);
        }
    }

    @Override
    public String getFileUrl(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage key cannot be null or empty for generating URL.");
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlDurationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();
            logger.debug("Generated pre-signed URL for {}: {}", storageKey, url);
            return url;
        } catch (S3Exception e) {
            logger.error("Error generating pre-signed URL for key {}: {}", storageKey, e.getMessage(), e);
            throw new StorageException("Could not generate pre-signed URL for file: " + storageKey, e);
        }
    }

    @Override
    public boolean delete(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            return false;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("Successfully deleted {} from S3 bucket {}", storageKey, bucketName);
            return true;
        } catch (S3Exception e) {
            logger.error("Error deleting file {} from S3: {}", storageKey, e.getMessage(), e);
            // Depending on policy, you might not want to throw exception if file didn't exist,
            // but for now, log and return false.
            return false;
        }
    }

    @Override
    public Path getRootLocation() {
        // This concept is less relevant for S3. S3 doesn't have a "root path" in the same way.
        // You could return null, or a Path representing the bucket (e.g., Paths.get("s3://"+bucketName)).
        // For now, returning null as it's not directly used by S3 operations in the same way as local.
        return null;
    }
    
    
}