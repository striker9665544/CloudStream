//src/main/java/com/cloudflix/backend/dto/request/WatchHistoryRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
// No Lombok here as we are doing manual getters/setters/constructors
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
public class WatchHistoryRequest {

    // videoId will likely be part of the path parameter in the controller,
    // but if you prefer to send it in the body, you can add it here:
    // @NotNull
    // private Long videoId;

    @NotNull(message = "Resume position cannot be null.")
    @Min(value = 0, message = "Resume position cannot be negative.")
    private Integer resumePositionSeconds;

    @NotNull(message = "Completed status cannot be null.")
    private Boolean completed;

    // --- Constructors ---
    public WatchHistoryRequest() {
    }

    public WatchHistoryRequest(Integer resumePositionSeconds, Boolean completed) {
        this.resumePositionSeconds = resumePositionSeconds;
        this.completed = completed;
    }

    // --- Getters ---
    public Integer getResumePositionSeconds() {
        return resumePositionSeconds;
    }

    public Boolean getCompleted() {
        return completed;
    }

    // --- Setters ---
    public void setResumePositionSeconds(Integer resumePositionSeconds) {
        this.resumePositionSeconds = resumePositionSeconds;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}