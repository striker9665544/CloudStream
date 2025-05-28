//src/main/java/com/cloudflix/backend/controller/FileUploadController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.service.FileStorageService;
import com.cloudflix.backend.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException; // <<< ADD IMPORT
import com.fasterxml.jackson.databind.ObjectMapper;      // <<< ADD IMPORT
import jakarta.servlet.http.HttpServletRequest;          // For logging if needed
import jakarta.validation.Valid; // Keep if you want to validate after deserialization
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// For manual validation if needed
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.ConstraintViolationException;
// import jakarta.validation.Validator;

import java.io.IOException;
// import java.util.Set; // For manual validation

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private ObjectMapper objectMapper; // Autowire ObjectMapper

    // Optional: Autowire Validator if you want to manually validate after deserialization
    // @Autowired
    // private Validator validator;

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<?> uploadVideo( // Changed return type to ResponseEntity<?> for more flexible error responses
            @RequestPart("videoFile") MultipartFile videoFile,
            @RequestPart("metadata") String metadataJsonString // <<< ACCEPT METADATA AS STRING
    ) {
        System.out.println("---- FileUploadController: inside uploadVideo ----");
        System.out.println("Received videoFile part: " + (videoFile != null ? videoFile.getOriginalFilename() : "null"));
        System.out.println("Received metadataJsonString: " + metadataJsonString);

        if (videoFile.isEmpty()) {
            System.err.println("FileUploadController: Video file is empty.");
            return ResponseEntity.badRequest().body("Video file cannot be empty."); // More informative
        }

        VideoMetadataRequest metadataRequest;
        try {
            // Manually deserialize the JSON string to VideoMetadataRequest DTO
            metadataRequest = objectMapper.readValue(metadataJsonString, VideoMetadataRequest.class);

            // Optional: Manually trigger validation if @Valid is not processed on String
            // Set<ConstraintViolation<VideoMetadataRequest>> violations = validator.validate(metadataRequest);
            // if (!violations.isEmpty()) {
            //     // Handle validation violations, e.g., return 400 with error messages
            //     System.err.println("FileUploadController: Metadata validation failed: " + violations);
            //     // You might want to build a proper error response DTO here
            //     return ResponseEntity.badRequest().body("Metadata validation failed: " + violations.iterator().next().getMessage());
            // }

        } catch (JsonProcessingException e) {
            System.err.println("FileUploadController: Failed to parse metadata JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid metadata format: " + e.getMessage());
        }

        try {
            String storedFileName = fileStorageService.storeFile(videoFile, metadataRequest.getTitle());
            VideoResponse videoResponse = videoService.createVideoMetadata(metadataRequest, storedFileName);
            System.out.println("FileUploadController: Video metadata created successfully.");
            return new ResponseEntity<>(videoResponse, HttpStatus.CREATED);

        } catch (IOException e) {
            System.err.println("FileUploadController: Failed to store video file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store video file.");
        } catch (IllegalArgumentException e) {
            System.err.println("FileUploadController: Illegal argument during upload: " + e.getMessage());
            return ResponseEntity.badRequest().body("Illegal argument: " + e.getMessage());
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("FileUploadController: Unexpected error during upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during upload.");
        }
    }
}