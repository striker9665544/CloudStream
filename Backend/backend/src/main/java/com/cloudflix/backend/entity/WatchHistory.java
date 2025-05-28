// src/main/java/com/cloudflix/backend/entity/WatchHistory.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
// import lombok.NoArgsConstructor; // <<< REMOVE THIS LOMBOK ANNOTATION
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects; // For a better equals/hashCode if needed

@Entity
@Table(name = "watch_history",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
// No @NoArgsConstructor here, we are defining it manually
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "watched_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime watchedAt;

    @Column(name = "resume_position_seconds", columnDefinition = "INT default 0")
    private int resumePositionSeconds = 0;

    @Column(name = "completed", columnDefinition = "BOOLEAN default FALSE")
    private boolean completed = false;

    // --- Constructors ---

    /**
     * Default constructor required by JPA.
     */
    public WatchHistory() {
        // JPA needs this, and it's good practice for it to be public.
    }

    public WatchHistory(User user, Video video) {
        this(); // Optionally call the default constructor
        this.user = user;
        this.video = video;
        this.watchedAt = LocalDateTime.now();
    }

    public WatchHistory(User user, Video video, LocalDateTime watchedAt, int resumePositionSeconds, boolean completed) {
        this(); // Optionally call the default constructor
        this.user = user;
        this.video = video;
        this.watchedAt = watchedAt;
        this.resumePositionSeconds = resumePositionSeconds;
        this.completed = completed;
    }

    // --- Getters and Setters ---
    // (Your existing manual getters and setters are fine)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }
    public LocalDateTime getWatchedAt() { return watchedAt; }
    public void setWatchedAt(LocalDateTime watchedAt) { this.watchedAt = watchedAt; }
    public int getResumePositionSeconds() { return resumePositionSeconds; }
    public void setResumePositionSeconds(int resumePositionSeconds) { this.resumePositionSeconds = resumePositionSeconds; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }


    // --- equals() and hashCode() ---
    // Consider basing equals/hashCode on 'id' if persisted, or the business key (user/video)
    // if not persisted yet. For persisted entities, 'id' is usually safest.
    // The one you have is based on user/video IDs, which is fine given the unique constraint.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchHistory that = (WatchHistory) o;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false; // Prefer ID if available
        // Fallback to user/video if ID is null (new entity)
        if (getId() == null) {
            if (getUser() != null ? (getUser().getId() == null || !getUser().getId().equals(that.getUser() != null ? that.getUser().getId() : null)) : that.getUser() != null) return false;
            return getVideo() != null ? (getVideo().getId() == null ||!getVideo().getId().equals(that.getVideo() != null ? that.getVideo().getId() : null)) : that.getVideo() == null;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : Objects.hash(getUser() != null ? getUser().getId() : null, getVideo() != null ? getVideo().getId() : null);
    }
}