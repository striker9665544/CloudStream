//src/main/java/com/cloudflix/backend/service/WatchHistoryService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.WatchHistoryRequest;
import com.cloudflix.backend.dto.response.WatchHistoryResponse;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.entity.WatchHistory;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.repository.VideoRepository;
import com.cloudflix.backend.repository.WatchHistoryRepository;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WatchHistoryService {

    @Autowired
    private WatchHistoryRepository watchHistoryRepository;

    @Autowired
    private UserRepository userRepository; // To fetch the User entity

    @Autowired
    private VideoRepository videoRepository; // To fetch the Video entity

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("User must be authenticated to perform this action.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }

    @Transactional
    public WatchHistoryResponse recordOrUpdateWatchProgress(Long videoId, WatchHistoryRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        WatchHistory watchHistory = watchHistoryRepository.findByUserAndVideo(currentUser, video)
                .orElseGet(() -> new WatchHistory(currentUser, video)); // Create new if not exists

        watchHistory.setResumePositionSeconds(request.getResumePositionSeconds());
        watchHistory.setCompleted(request.getCompleted());
        // watchedAt will be updated automatically by @UpdateTimestamp

        WatchHistory savedWatchHistory = watchHistoryRepository.save(watchHistory);
        return WatchHistoryResponse.fromEntity(savedWatchHistory);
    }

    @Transactional(readOnly = true)
    public Page<WatchHistoryResponse> getUserWatchHistory(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        Page<WatchHistory> historyPage = watchHistoryRepository.findAllByUserOrderByWatchedAtDesc(currentUser, pageable);
        return historyPage.map(WatchHistoryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<WatchHistoryResponse> getWatchProgressForVideo(Long videoId) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        return watchHistoryRepository.findByUserAndVideo(currentUser, video)
                .map(WatchHistoryResponse::fromEntity);
    }

    // Optional: Method to simply mark a video as completed
    @Transactional
    public WatchHistoryResponse markVideoAsCompleted(Long videoId) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        WatchHistory watchHistory = watchHistoryRepository.findByUserAndVideo(currentUser, video)
                .orElseGet(() -> new WatchHistory(currentUser, video));

        // If the video has a duration, setting resume position to duration implies completion.
        // Otherwise, just set completed to true.
        if (video.getDurationSeconds() != null && video.getDurationSeconds() > 0) {
            watchHistory.setResumePositionSeconds(video.getDurationSeconds());
        }
        watchHistory.setCompleted(true);

        WatchHistory savedWatchHistory = watchHistoryRepository.save(watchHistory);
        return WatchHistoryResponse.fromEntity(savedWatchHistory);
    }

    // Optional: Delete a specific entry from watch history
    @Transactional
    public void deleteWatchHistoryEntry(Long watchHistoryId) {
        User currentUser = getCurrentAuthenticatedUser();
        WatchHistory watchHistory = watchHistoryRepository.findById(watchHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchHistory", "id", watchHistoryId));

        // Ensure the current user owns this watch history entry
        if (!watchHistory.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to delete this watch history entry.");
        }
        watchHistoryRepository.delete(watchHistory);
    }

    // Optional: Clear all watch history for the current user
    @Transactional
    public void clearUserWatchHistory() {
        User currentUser = getCurrentAuthenticatedUser();
        // This is a bulk delete; depending on the JPA provider and DB, it might be less efficient
        // than iterating and deleting if cascading or other listeners are involved.
        // For simple cases, it's fine. Consider if you have many thousands of entries per user.
        // For now, we'll assume a simpler approach: fetch and delete.
        Page<WatchHistory> userHistory = watchHistoryRepository.findAllByUserOrderByWatchedAtDesc(currentUser, Pageable.unpaged());
        watchHistoryRepository.deleteAll(userHistory.getContent());
    }
}