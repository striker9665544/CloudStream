// src/main/java/com/cloudflix/backend/service/VideoService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.entity.ERole;
import com.cloudflix.backend.entity.Tag;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.TagRepository;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.repository.VideoRepository;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import com.cloudflix.backend.service.storage.CloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private static final String VIDEO_STATUS_AVAILABLE = "AVAILABLE";
    private static final String VIDEO_STATUS_PENDING = "PENDING_PROCESSING";
    private static final long CHUNK_SIZE = 1024 * 1024 * 2; // 2MB

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    //@Qualifier("localStorageService")
    private CloudStorageService cloudStorageService;

    private User getCurrentAuthenticatedUser() { // Helper method
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User must be authenticated for this operation.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }

    private void checkOwnershipOrAdmin(Long uploaderId) { // Helper for auth checks
        User currentUser = getCurrentAuthenticatedUser();
        boolean isAdmin = currentUser.getRoles().stream()
                            .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
        if (!isAdmin && !currentUser.getId().equals(uploaderId)) {
            throw new AccessDeniedException("User does not have permission to modify this resource.");
        }
    }


    @Transactional
    public VideoResponse createVideoMetadata(VideoMetadataRequest request, String storageKey) {
        User uploader = getCurrentAuthenticatedUser();
        Video video = new Video();
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setDurationSeconds(request.getDurationSeconds());
        video.setGenre(request.getGenre());
        video.setThumbnailUrl(request.getThumbnailUrl());
        video.setStorageObjectKey(storageKey);
        video.setUploader(uploader);
        video.setStatus(VIDEO_STATUS_PENDING);
        video.setViewCount(0L);
        handleTags(video, request.getTags());
        Video savedVideo = videoRepository.save(video);
        return VideoResponse.fromEntity(savedVideo);
    }

    @Transactional
    public VideoResponse updateVideoMetadata(Long videoId, VideoMetadataRequest request) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        
        // If non-admin, check ownership. Admins can edit any.
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName() == ERole.ROLE_ADMIN)) {
            if (video.getUploader() == null || !video.getUploader().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("User does not have permission to modify this video's metadata.");
            }
        }

        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setDurationSeconds(request.getDurationSeconds());
        video.setGenre(request.getGenre());
        video.setThumbnailUrl(request.getThumbnailUrl());
        handleTags(video, request.getTags());
        // @UpdateTimestamp handles updatedAt
        Video updatedVideo = videoRepository.save(video);
        return VideoResponse.fromEntity(updatedVideo);
    }

    @Transactional
    public VideoResponse updateVideoStatus(Long videoId, String status) {
        // This method is typically admin-only, so PreAuthorize on controller is primary.
        // Could add @PreAuthorize("hasRole('ADMIN')") here too for service-level.
        Video video = videoRepository.findById(videoId)
            .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        video.setStatus(status);
        if (VIDEO_STATUS_AVAILABLE.equals(status) && video.getProcessedTimestamp() == null) {
            video.setProcessedTimestamp(LocalDateTime.now());
        }
        Video updatedVideo = videoRepository.save(video);
        return VideoResponse.fromEntity(updatedVideo);
    }

    private void handleTags(Video video, Set<String> tagNames) {
        video.getTags().clear();
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> newTags = tagNames.stream()
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName.trim())
                                           .orElseGet(() -> tagRepository.save(new Tag(tagName.trim()))))
                .collect(Collectors.toSet());
            video.setTags(newTags);
        }
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> getAllAvailableVideos(Pageable pageable) {
        return videoRepository.findByStatus(VIDEO_STATUS_AVAILABLE, pageable)
                              .map(VideoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public VideoResponse getAvailableVideoById(Long videoId) {
        return videoRepository.findByIdAndStatus(videoId, VIDEO_STATUS_AVAILABLE)
                .map(VideoResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId + " (available)"));
    }
    
    @Transactional(readOnly = true)
    public Video getVideoEntityById(Long videoId) { // Used internally or by admin for raw entity
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> getAvailableVideosByGenre(String genre, Pageable pageable) {
        return videoRepository.findByGenreAndStatus(genre, VIDEO_STATUS_AVAILABLE, pageable)
                              .map(VideoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> getAvailableVideosByTag(String tagName, Pageable pageable) {
        return videoRepository.findByTagNameAndStatus(tagName, VIDEO_STATUS_AVAILABLE, pageable)
                              .map(VideoResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDistinctAvailableGenres() {
        return videoRepository.findDistinctGenres();
    }

    @Transactional(readOnly = true)
    public Page<VideoResponse> searchAvailableVideosByTitle(String title, Pageable pageable) {
        return videoRepository.findByTitleContainingIgnoreCaseAndStatus(title, VIDEO_STATUS_AVAILABLE, pageable)
                              .map(VideoResponse::fromEntity);
    }

    @Transactional
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        
        // Check ownership or admin role before deletion (PreAuthorize on controller handles this)
        // checkOwnershipOrAdmin(video.getUploader().getId()); // Or similar logic here for service layer protection

        if (video.getStorageObjectKey() != null && !video.getStorageObjectKey().isEmpty()) {
            cloudStorageService.delete(video.getStorageObjectKey());
        }
        videoRepository.delete(video);
    }

    @Transactional
    public void incrementViewCount(Long videoId) { // <<< THIS METHOD WAS MISSING OR MISMATCHED
        Video video = videoRepository.findById(videoId) // Fetch any video, not just available, to increment views
            .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        video.setViewCount((video.getViewCount() == null ? 0L : video.getViewCount()) + 1);
        videoRepository.save(video);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ResourceRegion> streamVideoFileWithRange(Long videoId, HttpHeaders headers) {
        // ... (The full streamVideoFileWithRange method as provided in the previous step) ...
        Video video = videoRepository.findByIdAndStatus(videoId, VIDEO_STATUS_AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId + " (not available or found for streaming)"));
        if (video.getStorageObjectKey() == null || video.getStorageObjectKey().trim().isEmpty()) {
            throw new ResourceNotFoundException("StorageKey", "for video id", videoId + " (key is missing)");
        }
        String storageKey = video.getStorageObjectKey();
        Resource videoResource = cloudStorageService.loadAsResource(storageKey);
        if (!videoResource.exists() || !videoResource.isReadable()) {
            throw new ResourceNotFoundException("Video File", "key", storageKey);
        }
        long resourceLength;
        try { resourceLength = videoResource.contentLength(); } 
        catch (IOException e) { throw new RuntimeException("Could not determine video length for video ID: " + videoId, e); }
        List<HttpRange> httpRanges = headers.getRange();
        ResourceRegion region = httpRanges.isEmpty() ?
                new ResourceRegion(videoResource, 0, Math.min(CHUNK_SIZE, resourceLength)) :
                HttpRange.toResourceRegions(httpRanges, videoResource).get(0);
        String contentType = "application/octet-stream";
        try {
            String filename = videoResource.getFilename();
            if (filename != null) {
                if (filename.toLowerCase().endsWith(".mp4")) contentType = "video/mp4";
                else if (filename.toLowerCase().endsWith(".webm")) contentType = "video/webm";
                else if (filename.toLowerCase().endsWith(".ogv")) contentType = "video/ogg";
            }
        } catch (Exception e) { /* ignore */ }
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<VideoResponse> getAllVideosForAdmin(Pageable pageable) {
        Page<Video> videosPage = videoRepository.findAll(pageable);
        return videosPage.map(VideoResponse::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public String getPlayableVideoUrl(Long videoId) {
        Video video = videoRepository.findByIdAndStatus(videoId, VIDEO_STATUS_AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId + " (not available or found)"));

        if (video.getStorageObjectKey() == null || video.getStorageObjectKey().trim().isEmpty()) {
            throw new ResourceNotFoundException("StorageKey", "for video id", videoId + " (key is missing)");
        }
        // CloudStorageService will return a pre-signed S3 URL if 'aws' profile is active,
        // or a local path (e.g., /api/files/filename.mp4) if 'local' profile is active.
        return cloudStorageService.getFileUrl(video.getStorageObjectKey());
    }
}