// src/main/java/com/cloudflix/backend/controller/FileUploadController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.service.storage.CloudStorageService;
import com.cloudflix.backend.service.VideoService;
import com.cloudflix.backend.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper; // Ensure this is still imported
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
// No need for StreamUtils or StandardCharsets if metadata is a String parameter

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final CloudStorageService cloudStorageService;
    private final VideoService videoService;
    private final ObjectMapper objectMapper; // Make sure this is autowired

    @Autowired
    public FileUploadController(
            CloudStorageService cloudStorageService,
            VideoService videoService,
            ObjectMapper objectMapper) { // Ensure ObjectMapper is injected
        this.cloudStorageService = cloudStorageService;
        this.videoService = videoService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<?> uploadVideo(
            @RequestPart("videoFile") MultipartFile videoFile,
            @RequestPart("metadata") String metadataJsonString
    ) {
        System.out.println("---- FileUploadController: inside uploadVideo (metadata as String) ----");
        System.out.println("Received videoFile part: " + (videoFile != null ? videoFile.getOriginalFilename() : "null"));
        System.out.println("Received metadataJsonString: " + metadataJsonString);

        if (videoFile == null || videoFile.isEmpty()) {
            System.err.println("FileUploadController: Video file is empty.");
            return ResponseEntity.badRequest().body("Video file cannot be empty.");
        }

        VideoMetadataRequest metadataRequest;
        try {
            // Manually deserialize the JSON string to VideoMetadataRequest DTO
            metadataRequest = objectMapper.readValue(metadataJsonString, VideoMetadataRequest.class);
            // TODO: Manual validation of metadataRequest if needed using Validator
        } catch (IOException e) { // Catch JsonProcessingException more specifically if possible
            System.err.println("FileUploadController: Failed to parse metadata JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid metadata format: " + e.getMessage());
        }

        try {
            String storedFileName = cloudStorageService.store(videoFile, metadataRequest.getTitle());
            System.out.println("FileUploadController: File stored as: " + storedFileName);

            VideoResponse videoResponse = videoService.createVideoMetadata(metadataRequest, storedFileName);
            System.out.println("FileUploadController: Video metadata created successfully for ID: " + videoResponse.getId());
            return new ResponseEntity<>(videoResponse, HttpStatus.CREATED);

        } catch (IOException | StorageException e) {
            System.err.println("FileUploadController: Failed to store video file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store video file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("FileUploadController: Illegal argument during upload: " + e.getMessage());
            return ResponseEntity.badRequest().body("Illegal argument: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("FileUploadController: Unexpected error during upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during upload.");
        }
    }
}