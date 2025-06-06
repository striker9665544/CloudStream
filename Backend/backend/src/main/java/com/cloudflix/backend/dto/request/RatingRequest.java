// src/main/java/com/cloudflix/backend/dto/request/RatingRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RatingRequest {

    @NotNull(message = "Rating value cannot be null.")
    @Min(value = 1, message = "Rating value must be at least 1.")
    @Max(value = 5, message = "Rating value must be at most 5.")
    private Short ratingValue; // Using Short to allow null initially, then validate with @NotNull

    // --- Constructors ---
    public RatingRequest() {
    }

    public RatingRequest(Short ratingValue) {
        this.ratingValue = ratingValue;
    }

    // --- Getter ---
    public Short getRatingValue() {
        return ratingValue;
    }

    // --- Setter ---
    public void setRatingValue(Short ratingValue) {
        this.ratingValue = ratingValue;
    }
}