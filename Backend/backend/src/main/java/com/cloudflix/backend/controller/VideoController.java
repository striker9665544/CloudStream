//src/main/java/com/cloudflix/backend/controller/VideoController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.entity.Video; // Make sure this is imported
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException; // Import for Files.probeContentType
import java.net.MalformedURLException;
import java.nio.file.Files; // Import for Files.probeContentType
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // This should resolve to the root of your 'uploads/videos' directory, e.g.,
    private final Path videoStorageLocation = Paths.get("uploads/videos").toAbsolutePath().normalize();

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<VideoResponse> createVideoMetadata(@Valid @RequestBody VideoMetadataRequest videoRequest) {
        // Generate a unique filename, DO NOT include the "uploads/videos/" prefix here.
        // The prefix is handled by videoStorageLocation when streaming.
        String uniqueFileName = System.currentTimeMillis() + "_" +
                videoRequest.getTitle()
                        .replaceAll("\\s+", "_") // Replace spaces with underscores
                        .replaceAll("[^a-zA-Z0-9._-]", "") + // Sanitize for typical filename characters
                ".mp4"; // Assuming mp4, adjust if needed

        // The videoService.createVideoMetadata will use this uniqueFileName
        // as the storageObjectKey when creating the Video entity.
        VideoResponse createdVideo = videoService.createVideoMetadata(videoRequest, uniqueFileName);
        return new ResponseEntity<>(createdVideo, HttpStatus.CREATED);
    }

    @PutMapping("/{videoId}")
    @PreAuthorize("hasRole('ADMIN') or @videoService.getVideoEntityById(#videoId).uploader.id == principal.id")
    public ResponseEntity<VideoResponse> updateVideoMetadata(@PathVariable Long videoId,
                                                           @Valid @RequestBody VideoMetadataRequest videoRequest) {
        VideoResponse updatedVideo = videoService.updateVideoMetadata(videoId, videoRequest);
        return ResponseEntity.ok(updatedVideo);
    }

    @PatchMapping("/{videoId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoResponse> updateVideoStatus(@PathVariable Long videoId, @RequestParam String status) {
        VideoResponse updatedVideo = videoService.updateVideoStatus(videoId, status);
        return ResponseEntity.ok(updatedVideo);
    }

    @GetMapping
    public ResponseEntity<Page<VideoResponse>> getAllAvailableVideos(
            @PageableDefault(size = 20, sort = "uploadTimestamp,desc") Pageable pageable) { // Added ,desc to sort
        Page<VideoResponse> videos = videoService.getAllAvailableVideos(pageable);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getAvailableVideoById(@PathVariable Long videoId) {
        VideoResponse video = videoService.getAvailableVideoById(videoId);
        videoService.incrementViewCount(videoId);
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
    @PreAuthorize("hasRole('ADMIN') or @videoService.getVideoEntityById(#videoId).uploader.id == principal.id")
    public ResponseEntity<MessageResponse> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok(new MessageResponse("Video deleted successfully."));
    }

    @PostMapping("/{videoId}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long videoId) {
        videoService.incrementViewCount(videoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long videoId) {
        try {
            System.out.println("[STREAM DEBUG] videoStorageLocation (absolute): " + this.videoStorageLocation.toString());

            Video video = videoService.getVideoEntityById(videoId);
            if (video == null || video.getStorageObjectKey() == null || video.getStorageObjectKey().isEmpty()) {
                System.err.println("[STREAM DEBUG] Video or storage key not found/empty for ID: " + videoId);
                return ResponseEntity.notFound().build();
            }

            String fileNameFromDb = video.getStorageObjectKey(); // This should now be just "Battlecruiser.mp4" or similar
            System.out.println("[STREAM DEBUG] storageKeyFromDb (should be filename only): " + fileNameFromDb);

            // Construct the full path to the video file
            Path videoFile = this.videoStorageLocation.resolve(fileNameFromDb).normalize();
            System.out.println("[STREAM DEBUG] Attempting to stream video from (resolved path): " + videoFile.toString());

            Resource resource = new UrlResource(videoFile.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "video/mp4"; // Default
                try {
                    String detectedContentType = Files.probeContentType(videoFile);
                    if (detectedContentType != null) {
                        contentType = detectedContentType;
                    }
                } catch (IOException e) {
                    System.err.println("[STREAM DEBUG] Could not determine content type for " + fileNameFromDb + ". Defaulting. Error: " + e.getMessage());
                }
                System.out.println("[STREAM DEBUG] Serving video with content type: " + contentType);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"") // Optional
                        .body(resource);
            } else {
                System.err.println("[STREAM DEBUG] Could not read video file (exists: " + resource.exists() + ", readable: " + resource.isReadable() + "): " + videoFile.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException ex) {
            System.err.println("[STREAM DEBUG] Malformed URL for video file (videoId: " + videoId + "): " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or a more specific error
        } catch (ResourceNotFoundException ex) { // If videoService.getVideoEntityById throws this
             System.err.println("[STREAM DEBUG] Video metadata not found for ID " + videoId + ": " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
         catch (Exception e) { // Catch-all for other unexpected errors
            System.err.println("[STREAM DEBUG] Generic error streaming video ID " + videoId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}