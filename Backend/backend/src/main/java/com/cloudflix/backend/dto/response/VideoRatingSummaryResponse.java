// src/main/java/com/cloudflix/backend/dto/response/VideoRatingSummaryResponse.java
package com.cloudflix.backend.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VideoRatingSummaryResponse {

    private Long videoId;
    private double averageRating; // Using double for average
    private long ratingCount;

    // --- Constructors ---
    public VideoRatingSummaryResponse() {
    }

    public VideoRatingSummaryResponse(Long videoId, Double averageRating, long ratingCount) {
        this.videoId = videoId;
        // Round the average rating to one decimal place if not null
        this.averageRating = (averageRating != null) ?
                BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        this.ratingCount = ratingCount;
    }

    // --- Getters ---
    public Long getVideoId() {
        return videoId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    // --- Setters ---
    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public void setAverageRating(double averageRating) {
        // Round the average rating to one decimal place
        this.averageRating = BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
    
    public void setAverageRating(Double averageRating) { // Overload to handle null Double from repository
        this.averageRating = (averageRating != null) ?
                BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }
}