//src/main/java/com/cloudflix/backend/controller/WatchHistoryController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.WatchHistoryRequest;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.dto.response.WatchHistoryResponse;
import com.cloudflix.backend.service.WatchHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600) // Or configure globally
@RestController
@RequestMapping("/api/history")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // All endpoints here require at least USER role
public class WatchHistoryController {

    @Autowired
    private WatchHistoryService watchHistoryService;

    // Endpoint to record or update watch progress for a specific video
    // Using PUT as it's idempotent for updating an existing resource or creating if not present.
    // Alternatively, POST could be used if you always treat it as "adding" an interaction.
    @PutMapping("/video/{videoId}")
    public ResponseEntity<WatchHistoryResponse> recordOrUpdateProgress(
            @PathVariable Long videoId,
            @Valid @RequestBody WatchHistoryRequest watchHistoryRequest) {
        WatchHistoryResponse response = watchHistoryService.recordOrUpdateWatchProgress(videoId, watchHistoryRequest);
        return ResponseEntity.ok(response);
    }

    // Endpoint to get the current authenticated user's watch history (paginated)
    @GetMapping("/user")
    public ResponseEntity<Page<WatchHistoryResponse>> getCurrentUserWatchHistory(
            @PageableDefault(size = 20, sort = "watchedAt,desc") Pageable pageable) {
        Page<WatchHistoryResponse> historyPage = watchHistoryService.getUserWatchHistory(pageable);
        return ResponseEntity.ok(historyPage);
    }

    // Endpoint to get the watch progress for a specific video for the current user
    @GetMapping("/video/{videoId}/progress")
    public ResponseEntity<WatchHistoryResponse> getWatchProgressForVideo(@PathVariable Long videoId) {
        Optional<WatchHistoryResponse> progress = watchHistoryService.getWatchProgressForVideo(videoId);
        return progress.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                     .body(null)); // Or a custom DTO indicating no history
    }

    // Optional: Endpoint to mark a video as fully watched
    @PostMapping("/video/{videoId}/complete")
    public ResponseEntity<WatchHistoryResponse> markVideoAsCompleted(@PathVariable Long videoId) {
        WatchHistoryResponse response = watchHistoryService.markVideoAsCompleted(videoId);
        return ResponseEntity.ok(response);
    }

    // Optional: Endpoint to delete a specific entry from watch history by its ID
    // (The watchHistoryId itself, not the videoId)
    @DeleteMapping("/{watchHistoryId}")
    public ResponseEntity<MessageResponse> deleteWatchHistoryEntry(@PathVariable Long watchHistoryId) {
        watchHistoryService.deleteWatchHistoryEntry(watchHistoryId);
        return ResponseEntity.ok(new MessageResponse("Watch history entry deleted successfully."));
    }

    // Optional: Endpoint to clear all watch history for the current user
    @DeleteMapping("/user/clear")
    public ResponseEntity<MessageResponse> clearUserWatchHistory() {
        watchHistoryService.clearUserWatchHistory();
        return ResponseEntity.ok(new MessageResponse("Watch history cleared successfully."));
    }
}