//src/main/java/com/cloudflix/backend/dto/request/VideoMetadataRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class VideoMetadataRequest {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    private Integer durationSeconds;

    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    private String genre;

    // The actual file upload will be separate, this DTO is for metadata.
    // StorageObjectKey will likely be set by the upload service.
    // private String storageObjectKey;

    @Size(max = 1024, message = "Thumbnail URL cannot exceed 1024 characters")
    private String thumbnailUrl;

    private Set<String> tags;
    
    // --- Empty (No-Argument) Constructor ---
    // Essential for frameworks like Spring MVC/REST and Jackson for deserialization
    public VideoMetadataRequest() {}


    // --- All-Arguments Constructor ---
    // Constructor taking all fields as arguments
    // Useful for easily creating instances with all data filled
    public VideoMetadataRequest(String title, String description, Integer durationSeconds,String genre, String thumbnailUrl, Set<String> tags) { 
        this.title = title;
        this.description = description;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
        this.thumbnailUrl = thumbnailUrl;
        this.tags = tags;
    }


    // --- Getters ---

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public String getGenre() {
        return genre;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Set<String> getTags() {
        return tags;
    }


    // --- Setters ---

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    // --- Helper methods for managing the Set<String> (optional but often useful) ---
    // Instead of replacing the entire set, you might want methods to add/remove individual tags
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>(); // Ensure the set is initialized if needed
        }
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
         if (this.tags == null) {
            return; // Nothing to remove from
        }
        this.tags.remove(tag);
    }
}