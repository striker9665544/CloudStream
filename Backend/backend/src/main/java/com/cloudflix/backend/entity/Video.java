// src/main/java/com/cloudflix/backend/entity/Video.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "videos") // Matches your SQL schema
@NoArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob // For longer text
    private String description;

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // e.g., length of the video in seconds

    @Column(length = 100) // Add a genre field
    private String genre;

    // This will be the S3 key or a path to the video file
    @Column(name = "storage_object_key", nullable = false, length = 1024)
    private String storageObjectKey;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;

    @Column(name = "hls_manifest_url", length = 1024)
    private String hlsManifestUrl; // For HLS streaming later

    @Column(name = "upload_timestamp", updatable = false)
    @CreationTimestamp // Handled by Hibernate
    private LocalDateTime uploadTimestamp;

    @Column(name = "processed_timestamp")
    private LocalDateTime processedTimestamp;

    @Column(length = 50)
    private String status; // E.g., PENDING_PROCESSING, AVAILABLE, PROCESSING_FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_user_id")
    private User uploader;

    @Column(name = "view_count", nullable = false, columnDefinition = "BIGINT default 0")
    private Long viewCount = 0L;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "video_tags",
               joinColumns = @JoinColumn(name = "video_id"),
               inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();


    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Video() {}
    
    // Constructor for essential fields (others can be set via setters or default)
    public Video(String title, String description, String storageObjectKey, User uploader, String genre) {
        this.title = title;
        this.description = description;
        this.storageObjectKey = storageObjectKey;
        this.uploader = uploader;
        this.status = "PENDING_PROCESSING"; // Default status
        this.genre = genre;
    }

    // Helper methods for managing tags
    //public void addTag(Tag tag) {
    //    this.tags.add(tag);
    //    tag.getVideos().add(this);
    //}

    //public void removeTag(Tag tag) {
    //    this.tags.remove(tag);
    //    tag.getVideos().remove(this);
    //}
    
 // Add these methods below your field declarations

    // --- Getters ---

    public Long getId() {
        return id;
    }

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

    public String getStorageObjectKey() {
        return storageObjectKey;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getHlsManifestUrl() {
        return hlsManifestUrl;
    }

    public LocalDateTime getUploadTimestamp() {
        return uploadTimestamp;
    }

    public LocalDateTime getProcessedTimestamp() {
        return processedTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public User getUploader() {
        return uploader;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    // --- Setters ---
    // Note: ID, uploadTimestamp, createdAt, and updatedAt are often not
    // set directly after creation, as they are managed by the database or Hibernate.
    // I've included setters for completeness, but you might choose not to use them
    // or remove them if they shouldn't be modified after initial persistence.

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setStorageObjectKey(String storageObjectKey) {
        this.storageObjectKey = storageObjectKey;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setHlsManifestUrl(String hlsManifestUrl) {
        this.hlsManifestUrl = hlsManifestUrl;
    }

    // Setter for uploadTimestamp (managed by @CreationTimestamp, so set might be discouraged)
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public void setProcessedTimestamp(LocalDateTime processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    // Setter for createdAt (managed by @CreationTimestamp, so set might be discouraged)
     public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Setter for updatedAt (managed by @UpdateTimestamp, so set might be discouraged)
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Helper methods for managing the Set<Tag> (optional but often useful) ---
    // Instead of replacing the entire set, you might want methods to add/remove individual tags
    public void addTag(Tag tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>(); // Ensure the set is initialized
        }
        this.tags.add(tag);
        tag.getVideos().add(this); // Assuming Tag has a corresponding many-to-many field 'videos'
    }

    public void removeTag(Tag tag) {
         if (this.tags == null) {
            return; // Nothing to remove from
        }
        this.tags.remove(tag);
        tag.getVideos().remove(this); // Assuming Tag has a corresponding many-to-many field 'videos'
    }
}