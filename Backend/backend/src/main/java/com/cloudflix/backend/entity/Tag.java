// src/main/java/com/cloudflix/backend/entity/Tag.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // For proper Set behavior

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags") // Matches your SQL schema
@NoArgsConstructor
@EqualsAndHashCode(exclude = "videos") // Important for ManyToMany relationships
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Video> videos = new HashSet<>();
    
    public Tag() {}
    
    public Tag(String name) { // <--- THIS CONSTRUCTOR
        this.name = name;
    }

 // Add these methods below your field declarations

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Video> getVideos() {
        return videos;
    }


    // --- Setters ---
    // Note: The 'id' field is managed by the database (@GeneratedValue),
    // so the setter is generally not used in application code.
    // I've included it for completeness, but you might omit it.

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Note: Setting the entire 'videos' set can be done, but it's often
    // more robust to use helper methods (addVideo, removeVideo) to
    // manage the relationship correctly on both sides (Tag and Video).
    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }


    // --- Helper methods for managing the Set<Video> (recommended for many-to-many) ---
    // These methods help keep the relationship consistent on both sides.

    public void addVideo(Video video) {
        if (this.videos == null) {
            this.videos = new HashSet<>(); // Ensure the set is initialized
        }
        this.videos.add(video);
        // Also update the 'tags' set on the Video object if it's bidirectional
        // This assumes the 'Video' class has a field like 'Set<Tag> tags'.
        if (video.getTags() == null) {
             // Handle case where video.tags is null if necessary
             // Or rely on the addTag method on Video if available
        }
        video.getTags().add(this);
    }

    public void removeVideo(Video video) {
        if (this.videos == null) {
            return; // Nothing to remove from
        }
        this.videos.remove(video);
        // Also update the 'tags' set on the Video object
        if (video.getTags() != null) {
             video.getTags().remove(this);
        }
    }
}