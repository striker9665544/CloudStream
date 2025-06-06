// src/main/java/com/cloudflix/backend/service/RatingService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.RatingRequest;
import com.cloudflix.backend.dto.response.RatingResponse;
import com.cloudflix.backend.dto.response.VideoRatingSummaryResponse;
import com.cloudflix.backend.entity.Rating;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.RatingRepository;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.repository.VideoRepository;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User must be authenticated to perform this action.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }

    @Transactional
    public RatingResponse addOrUpdateRating(Long videoId, RatingRequest ratingRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findByIdAndStatus(videoId, "AVAILABLE") // Only rate available videos
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        // Check if the user has already rated this video
        Rating rating = ratingRepository.findByUserAndVideo(currentUser, video)
                .orElseGet(() -> new Rating(currentUser, video, ratingRequest.getRatingValue())); // Create new if not exists

        // Update rating value if it already exists or set for new
        rating.setRatingValue(ratingRequest.getRatingValue());
        // createdAt and updatedAt will be handled by annotations

        Rating savedRating = ratingRepository.save(rating);
        return RatingResponse.fromEntity(savedRating);
    }

    @Transactional(readOnly = true)
    public Optional<RatingResponse> getUserRatingForVideo(Long videoId) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findById(videoId) // No need to check status here, user might have rated before it became unavailable
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        return ratingRepository.findByUserAndVideo(currentUser, video)
                .map(RatingResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public VideoRatingSummaryResponse getVideoRatingSummary(Long videoId) {
        Video video = videoRepository.findById(videoId) // Can get summary even if video status changes
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        Double averageRating = ratingRepository.findAverageRatingByVideo(video);
        long ratingCount = ratingRepository.countByVideo(video);

        return new VideoRatingSummaryResponse(videoId, averageRating, ratingCount);
    }

    @Transactional
    public void deleteRating(Long videoId) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        Rating rating = ratingRepository.findByUserAndVideo(currentUser, video)
                .orElseThrow(() -> new ResourceNotFoundException("Rating", "for user " + currentUser.getId() + " and video " + videoId, "not found"));
        
        // No explicit ownership check needed here as findByUserAndVideo already scopes it to the current user.
        // If an admin were to delete any rating, they'd need a different service method taking ratingId.
        ratingRepository.delete(rating);
    }

    // Optional: Admin method to delete any rating by its ID
    /*
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRatingById(Long ratingId) {
        if (!ratingRepository.existsById(ratingId)) {
            throw new ResourceNotFoundException("Rating", "id", ratingId);
        }
        ratingRepository.deleteById(ratingId);
    }
    */
}