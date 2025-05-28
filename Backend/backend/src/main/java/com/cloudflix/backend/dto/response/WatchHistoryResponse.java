//src/main/java/com/cloudflix/backend/dto/response/WatchHistoryResponse.java
package com.cloudflix.backend.dto.response;

import com.cloudflix.backend.entity.WatchHistory;
import java.time.LocalDateTime;
// No Lombok here as we are doing manual getters/setters/constructors
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor // If you were using Lombok
public class WatchHistoryResponse {

    private Long watchHistoryId; // The ID of the watch history record itself
    private VideoSummaryResponse video; // Nested DTO for video summary
    private LocalDateTime watchedAt;
    private int resumePositionSeconds;
    private boolean completed;

    // --- Constructors ---
    public WatchHistoryResponse() {}

    public WatchHistoryResponse(Long watchHistoryId, VideoSummaryResponse video, LocalDateTime watchedAt, int resumePositionSeconds, boolean completed) {
        this.watchHistoryId = watchHistoryId;
        this.video = video;
        this.watchedAt = watchedAt;
        this.resumePositionSeconds = resumePositionSeconds;
        this.completed = completed;
    }


    // Static factory method to convert WatchHistory entity to WatchHistoryResponse DTO
    public static WatchHistoryResponse fromEntity(WatchHistory watchHistory) {
        if (watchHistory == null) {
            return null;
        }
        return new WatchHistoryResponse(
                watchHistory.getId(),
                VideoSummaryResponse.fromVideoEntity(watchHistory.getVideo()), // Use the nested DTO's factory
                watchHistory.getWatchedAt(),
                watchHistory.getResumePositionSeconds(),
                watchHistory.isCompleted()
        );
    }

    // --- Getters ---
    public Long getWatchHistoryId() {
        return watchHistoryId;
    }

    public VideoSummaryResponse getVideo() {
        return video;
    }

    public LocalDateTime getWatchedAt() {
        return watchedAt;
    }

    public int getResumePositionSeconds() {
        return resumePositionSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    // --- Setters ---
    public void setWatchHistoryId(Long watchHistoryId) {
        this.watchHistoryId = watchHistoryId;
    }

    public void setVideo(VideoSummaryResponse video) {
        this.video = video;
    }

    public void setWatchedAt(LocalDateTime watchedAt) {
        this.watchedAt = watchedAt;
    }

    public void setResumePositionSeconds(int resumePositionSeconds) {
        this.resumePositionSeconds = resumePositionSeconds;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    // --- Nested DTO for Video Summary ---
    // This avoids sending the full VideoResponse and potentially circular dependencies.
    // It includes just enough info for a watch history list item.
    // No Lombok here as we are doing manual getters/setters/constructors
    // @Data
    // @NoArgsConstructor
    // @AllArgsConstructor // If you were using Lombok
    public static class VideoSummaryResponse {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private Integer durationSeconds; // Optional, but useful for progress bars
        private String genre; // Optional

        // --- Constructors for VideoSummaryResponse ---
        public VideoSummaryResponse() {}

        public VideoSummaryResponse(Long id, String title, String thumbnailUrl, Integer durationSeconds, String genre) {
            this.id = id;
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
            this.durationSeconds = durationSeconds;
            this.genre = genre;
        }

        // Factory method for VideoSummaryResponse
        public static VideoSummaryResponse fromVideoEntity(com.cloudflix.backend.entity.Video videoEntity) {
            if (videoEntity == null) {
                return null;
            }
            return new VideoSummaryResponse(
                    videoEntity.getId(),
                    videoEntity.getTitle(),
                    videoEntity.getThumbnailUrl(),
                    videoEntity.getDurationSeconds(),
                    videoEntity.getGenre()
            );
        }

        // --- Getters for VideoSummaryResponse ---
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public Integer getDurationSeconds() { return durationSeconds; }
        public String getGenre() { return genre; }

        // --- Setters for VideoSummaryResponse ---
        public void setId(Long id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
        public void setGenre(String genre) { this.genre = genre; }
    }
}