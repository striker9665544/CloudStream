// src/main/java/com/cloudflix/backend/controller/AdminVideoController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/videos") // Base path for admin video operations
@PreAuthorize("hasRole('ADMIN')")    // All methods in this controller require ADMIN role
public class AdminVideoController {

    @Autowired
    private VideoService videoService;

    // Endpoint for Admins to List All Videos (any status)
    @GetMapping
    public ResponseEntity<Page<VideoResponse>> getAllVideos(
    		@PageableDefault(size = 20, sort = "uploadTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<VideoResponse> videos = videoService.getAllVideosForAdmin(pageable);
        return ResponseEntity.ok(videos);
    }

    // Endpoint for Admins to Change Video Status
    // (Moved from VideoController for admin-specific scope, or keep there if preferred)
    @PatchMapping("/{videoId}/status")
    public ResponseEntity<VideoResponse> updateVideoStatus(@PathVariable Long videoId, @RequestParam String status) {
        // The PreAuthorize on the class already ensures only ADMINs can call this.
        // The service method updateVideoStatus itself might not need an additional @PreAuthorize
        // if it's only ever called from admin-secured controllers.
        VideoResponse updatedVideo = videoService.updateVideoStatus(videoId, status);
        return ResponseEntity.ok(updatedVideo);
    }

    // Endpoint for Admins to Edit Any Video's Metadata
    // (Moved from VideoController for admin-specific scope, or keep there if preferred)
    @PutMapping("/{videoId}")
    public ResponseEntity<VideoResponse> updateAnyVideoMetadata(@PathVariable Long videoId,
                                                               @Valid @RequestBody VideoMetadataRequest videoRequest) {
        // The PreAuthorize on the class ensures only ADMINs.
        // VideoService.updateVideoMetadata can handle the update.
        // If updateVideoMetadata has ownership checks, ensure admins bypass them or create an admin-specific service method.
        // Assuming VideoService.updateVideoMetadata is flexible enough or has an admin path.
        VideoResponse updatedVideo = videoService.updateVideoMetadata(videoId, videoRequest);
        return ResponseEntity.ok(updatedVideo);
    }

    // Note: Deletion might also move here, or stay in VideoController if owners can also delete.
    // If moving Video Deletion for Admins:
    /*
    @DeleteMapping("/{videoId}")
    public ResponseEntity<MessageResponse> deleteAnyVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId); // Ensure deleteVideo service method allows admin to delete any video
        return ResponseEntity.ok(new MessageResponse("Video deleted successfully by admin."));
    }
    */
    
    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    //      ENSURE THIS DELETE MAPPING IS PRESENT AND CORRECT
    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    @DeleteMapping("/{videoId}")
    public ResponseEntity<MessageResponse> deleteVideo(@PathVariable Long videoId) {
        // The class-level @PreAuthorize("hasRole('ADMIN')") already secures this.
        // The VideoService.deleteVideo method should handle deleting from storage (S3/local) and DB.
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok(new MessageResponse("Video (ID: " + videoId + ") deleted successfully by admin."));
    }
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //      END OF DELETE MAPPING
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
}