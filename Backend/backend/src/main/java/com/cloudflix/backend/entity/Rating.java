// src/main/java/com/cloudflix/backend/entity/Rating.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Min(value = 1, message = "Rating value must be at least 1")
    @Max(value = 5, message = "Rating value must be at most 5")
    @Column(name = "rating_value", nullable = false)
    private short ratingValue; // Using short for SMALLINT, can also use Integer

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public Rating() {
        // JPA No-Arg Constructor
    }

    public Rating(User user, Video video, short ratingValue) {
        this.user = user;
        this.video = video;
        this.ratingValue = ratingValue;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public short getRatingValue() { return ratingValue; }
    public void setRatingValue(short ratingValue) { this.ratingValue = ratingValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- equals() and hashCode() ---
    // Based on user and video as they form the unique business key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        // If IDs are available and non-null, they are the best for equality
        if (id != null && rating.id != null) {
            return id.equals(rating.id);
        }
        // Otherwise, use the business key (user and video)
        // Ensure user and video themselves have proper equals/hashCode based on their IDs
        if (!Objects.equals(user != null ? user.getId() : null, rating.user != null ? rating.user.getId() : null)) return false;
        return Objects.equals(video != null ? video.getId() : null, rating.video != null ? rating.video.getId() : null);
    }

    @Override
    public int hashCode() {
        // If ID is available, use it for hashcode
        if (id != null) {
            return Objects.hash(id);
        }
        // Otherwise, use the business key
        return Objects.hash(user != null ? user.getId() : null, video != null ? video.getId() : null);
    }
}