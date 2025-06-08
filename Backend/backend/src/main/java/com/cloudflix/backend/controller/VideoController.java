// src/main/java/com/cloudflix/backend/controller/VideoController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.exception.ResourceNotFoundException;
// import com.cloudflix.backend.entity.Video; // No longer directly used here for streaming logic
import com.cloudflix.backend.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;         // Keep for other potential uses if any
import org.springframework.core.io.support.ResourceRegion; // <<< ADD THIS IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;         // <<< ADD THIS IMPORT
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;        // <<< ADD THIS IMPORT
import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType; // No longer directly used here for streaming
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
// import java.io.IOException; // No longer directly used here
// import java.net.MalformedURLException; // No longer directly used here
// import java.nio.file.Files; // No longer directly used here
// import java.nio.file.Path; // No longer directly used here
// import java.nio.file.Paths; // No longer directly used here
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // REMOVE THIS - Path logic is now in LocalStorageServiceImpl via CloudStorageService
    // private final Path videoStorageLocation = Paths.get("uploads/videos").toAbsolutePath().normalize();

    @PostMapping // This endpoint is for creating metadata *after* a file upload
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')") // Assuming this endpoint is still used by FileUploadController indirectly
    public ResponseEntity<VideoResponse> createVideoMetadata(@Valid @RequestBody VideoMetadataRequest videoRequest) {
        // Note: The actual file upload now happens via FileUploadController.
        // This createVideoMetadata in VideoController might become an internal helper
        // or only callable by FileUploadController, or removed if FileUploadController calls
        // videoService.createVideoMetadata directly.
        // For now, let's assume it might still be used for some metadata-only creation scenario
        // or the FileUploadController calls VideoService.createVideoMetadata.
        
        // If this endpoint is for metadata-only and storageObjectKey is set manually:
        String uniqueFileName = videoRequest.getTitle() != null ? // A simplified placeholder for storageObjectKey if not set
                System.currentTimeMillis() + "_" +
                videoRequest.getTitle().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9._-]", "") + ".mp4"
                : "default_video_key_" + System.currentTimeMillis() + ".mp4";

        VideoResponse createdVideo = videoService.createVideoMetadata(videoRequest, uniqueFileName /* or get this from request if it contains storageKey */);
        return new ResponseEntity<>(createdVideo, HttpStatus.CREATED);
    }


    @PutMapping("/{videoId}")
    @PreAuthorize("hasRole('ADMIN') or @videoService.getVideoEntityById(#videoId).uploader.id == principal.id")
    public ResponseEntity<VideoResponse> updateVideoMetadata(@PathVariable Long videoId,
                                                           @Valid @RequestBody VideoMetadataRequest videoRequest) {
        VideoResponse updatedVideo = videoService.updateVideoMetadata(videoId, videoRequest);
        return ResponseEntity.ok(updatedVideo);
    }

    // Note: The @PatchMapping and @PreAuthorize here were for VideoController.
    // If these are meant for Admin only, they should be in AdminVideoController.
    // For now, keeping them here if general users (owners) could potentially change status via other means.
    // However, typical status changes are admin tasks.
    @PatchMapping("/{videoId}/status")
    @PreAuthorize("hasRole('ADMIN')") // More likely an admin task
    public ResponseEntity<VideoResponse> updateVideoStatus(@PathVariable Long videoId, @RequestParam String status) {
        VideoResponse updatedVideo = videoService.updateVideoStatus(videoId, status);
        return ResponseEntity.ok(updatedVideo);
    }

    @GetMapping
    public ResponseEntity<Page<VideoResponse>> getAllAvailableVideos(
            // Corrected @PageableDefault
            @PageableDefault(size = 20, sort = "uploadTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<VideoResponse> videos = videoService.getAllAvailableVideos(pageable);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getAvailableVideoById(@PathVariable Long videoId) {
        VideoResponse video = videoService.getAvailableVideoById(videoId);
        videoService.incrementViewCount(videoId); // Increment view count
        return ResponseEntity.ok(video);
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<Page<VideoResponse>> getAvailableVideosByGenre(@PathVariable String genreName,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        Page<VideoResponse> videos = videoService.getAvailableVideosByGenre(genreName, pageable);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<Page<VideoResponse>> getAvailableVideosByTag(@PathVariable String tagName,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        Page<VideoResponse> videos = videoService.getAvailableVideosByTag(tagName, pageable);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getDistinctAvailableGenres() {
        List<String> genres = videoService.getDistinctAvailableGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VideoResponse>> searchAvailableVideosByTitle(@RequestParam String title,
                                                                          @PageableDefault(size = 20) Pageable pageable) {
        Page<VideoResponse> videos = videoService.searchAvailableVideosByTitle(title, pageable);
        return ResponseEntity.ok(videos);
    }

    @DeleteMapping("/{videoId}")
    //@PreAuthorize("hasRole('ADMIN') or @videoService.getVideoEntityById(#videoId).uploader.id == principal.id")
    public ResponseEntity<MessageResponse> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId); // This now calls cloudStorageService.delete internally
        return ResponseEntity.ok(new MessageResponse("Video deleted successfully."));
    }

    @PostMapping("/{videoId}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long videoId) {
        videoService.incrementViewCount(videoId);
        return ResponseEntity.ok().build();
    }

    // === UPDATED STREAMING ENDPOINT ===
    /*@GetMapping("/stream/{videoId}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long videoId,
            @RequestHeader HttpHeaders headers) { // Inject HttpHeaders to get the Range header
        return videoService.streamVideoFileWithRange(videoId, headers);
    }*/
    
    // Endpoint to get the streamable URL (could be pre-signed S3 or local path)
    @GetMapping("/{videoId}/stream-url")
    // Security: If videos are public, this can be public.
    // If only authenticated users can play, add @PreAuthorize("isAuthenticated()")
    // For now, aligns with GET /api/videos/** being permitAll
    public ResponseEntity<Object> getStreamUrl(@PathVariable Long videoId) {
        try {
            String streamUrl = videoService.getPlayableVideoUrl(videoId);
            // Return as a simple JSON object
            return ResponseEntity.ok(java.util.Collections.singletonMap("url", streamUrl));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            // Log error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error generating stream URL."));
        }
    }

    // Your existing @GetMapping("/stream/{videoId}") that returns ResponseEntity<ResourceRegion>
    // will now PRIMARILY serve local files when the "local" profile is active,
    // or if you decide to stream S3 through your backend (less ideal).
    // For S3, the frontend will use the URL from "/stream-url".
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long videoId,
            @RequestHeader HttpHeaders headers) {
        // This will call videoService.streamVideoFileWithRange, which uses
        // cloudStorageService.loadAsResource(). For S3, loadAsResource might
        // be less efficient than using a presigned URL directly.
        // This endpoint becomes the fallback or local streaming handler.
        return videoService.streamVideoFileWithRange(videoId, headers);
    }
}