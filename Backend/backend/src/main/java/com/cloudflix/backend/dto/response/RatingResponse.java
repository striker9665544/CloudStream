// src/main/java/com/cloudflix/backend/dto/response/RatingResponse.java
package com.cloudflix.backend.dto.response;

import com.cloudflix.backend.entity.Rating;
import java.time.LocalDateTime;

public class RatingResponse {

    private Long id;
    private Long videoId;
    private Long userId;    // Could also be a simplified UserInfoResponse if needed
    private short ratingValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public RatingResponse() {
    }

    public RatingResponse(Long id, Long videoId, Long userId, short ratingValue, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.videoId = videoId;
        this.userId = userId;
        this.ratingValue = ratingValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Static factory method
    public static RatingResponse fromEntity(Rating rating) {
        if (rating == null) {
            return null;
        }
        return new RatingResponse(
                rating.getId(),
                rating.getVideo() != null ? rating.getVideo().getId() : null,
                rating.getUser() != null ? rating.getUser().getId() : null,
                rating.getRatingValue(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }

    // --- Getters ---
    public Long getId() { return id; }
    public Long getVideoId() { return videoId; }
    public Long getUserId() { return userId; }
    public short getRatingValue() { return ratingValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setRatingValue(short ratingValue) { this.ratingValue = ratingValue; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}