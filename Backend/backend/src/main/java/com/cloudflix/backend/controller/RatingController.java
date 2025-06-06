// src/main/java/com/cloudflix/backend/controller/RatingController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.RatingRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.RatingResponse;
import com.cloudflix.backend.dto.response.VideoRatingSummaryResponse;
import com.cloudflix.backend.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api") // General base path
public class RatingController {

    @Autowired
    private RatingService ratingService;

    // Add or Update a rating for a specific video
    // User must be authenticated to rate.
    @PutMapping("/videos/{videoId}/ratings") // Using PUT as it's often an update or create-if-not-exists
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RatingResponse> addOrUpdateRating(
            @PathVariable Long videoId,
            @Valid @RequestBody RatingRequest ratingRequest) {
        RatingResponse ratingResponse = ratingService.addOrUpdateRating(videoId, ratingRequest);
        return ResponseEntity.ok(ratingResponse);
    }

    // Get the current authenticated user's rating for a specific video
    @GetMapping("/videos/{videoId}/ratings/my-rating")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RatingResponse> getUserRatingForVideo(@PathVariable Long videoId) {
        Optional<RatingResponse> ratingResponse = ratingService.getUserRatingForVideo(videoId);
        return ratingResponse.map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                           .body(null)); // Or custom DTO for "not rated yet"
    }

    // Get the average rating and count for a specific video (publicly accessible)
    @GetMapping("/videos/{videoId}/ratings/summary")
    public ResponseEntity<VideoRatingSummaryResponse> getVideoRatingSummary(@PathVariable Long videoId) {
        VideoRatingSummaryResponse summaryResponse = ratingService.getVideoRatingSummary(videoId);
        return ResponseEntity.ok(summaryResponse);
    }

    // Delete the current authenticated user's rating for a specific video
    @DeleteMapping("/videos/{videoId}/ratings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteUserRatingForVideo(@PathVariable Long videoId) {
        ratingService.deleteRating(videoId);
        return ResponseEntity.ok(new MessageResponse("Your rating for the video has been removed."));
    }

    // --- Optional Admin Endpoints ---
    /*
    @DeleteMapping("/admin/ratings/{ratingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAnyRatingById(@PathVariable Long ratingId) {
        ratingService.deleteRatingById(ratingId); // Assuming this method exists in service
        return ResponseEntity.ok(new MessageResponse("Rating with ID " + ratingId + " deleted successfully."));
    }
    */
}