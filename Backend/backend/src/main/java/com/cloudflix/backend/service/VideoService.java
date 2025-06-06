//src/main/java/com/cloudflix/backend/service/VideoService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.VideoMetadataRequest;
import com.cloudflix.backend.dto.response.VideoResponse;
import com.cloudflix.backend.entity.Tag;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.TagRepository;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.repository.VideoRepository;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource; // <<< ADD THIS IMPORT
import org.springframework.core.io.Resource;            // <<< ADD THIS IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;                // <<< ADD THIS IMPORT
import org.springframework.http.ResponseEntity;           // <<< ADD THIS IMPORT
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;                             // <<< ADD THIS IMPORT
import java.nio.file.Paths;                            // <<< ADD THIS IMPORT
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private static final String VIDEO_STATUS_AVAILABLE = "AVAILABLE";
    private static final String VIDEO_STATUS_PENDING = "PENDING_PROCESSING";

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;

    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    //          ADD THIS FIELD FOR VIDEO STORAGE LOCATION
    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    // Base path where uploaded videos are stored (CONFIGURE THIS IN application.properties LATER IF NEEDED)
    // For now, assumes a directory named "uploaded_videos" in your project's running directory.
    private final Path videoStorageLocation = Paths.get("uploaded_videos").toAbsolutePath().normalize();
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //          END OF ADDED FIELD
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


    // --- Create/Update Operations ---
    // ... (your existing createVideoMetadata, updateVideoMetadata, updateVideoStatus, handleTags methods are fine here) ...
    @Transactional
    public VideoResponse createVideoMetadata(VideoMetadataRequest request, String uniqueFileName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User uploader = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Video video = new Video();
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setDurationSeconds(request.getDurationSeconds());
        video.setGenre(request.getGenre());
        video.setThumbnailUrl(request.getThumbnailUrl());

        video.setStorageObjectKey(uniqueFileName); 
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
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setDurationSeconds(request.getDurationSeconds());
        video.setGenre(request.getGenre());
        video.setThumbnailUrl(request.getThumbnailUrl());
        handleTags(video, request.getTags());
        video.setUpdatedAt(LocalDateTime.now());
        Video updatedVideo = videoRepository.save(video);
        return VideoResponse.fromEntity(updatedVideo);
    }

    @Transactional
    public VideoResponse updateVideoStatus(Long videoId, String status) {
        Video video = videoRepository.findById(videoId)
            .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        video.setStatus(status);
        if (VIDEO_STATUS_AVAILABLE.equals(status)) {
            video.setProcessedTimestamp(LocalDateTime.now());
        }
        Video updatedVideo = videoRepository.save(video);
        return VideoResponse.fromEntity(updatedVideo);
    }

    private void handleTags(Video video, Set<String> tagNames) {
        video.getTags().clear();
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> newTags = new HashSet<>();
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName.trim())));
                newTags.add(tag);
            }
            video.setTags(newTags);
        }
    }

    // --- Read Operations ---
    // ... (your existing getAllAvailableVideos, getAvailableVideoById, getVideoEntityById, etc. are fine here) ...
    public Page<VideoResponse> getAllAvailableVideos(Pageable pageable) {
        Page<Video> videosPage = videoRepository.findByStatus(VIDEO_STATUS_AVAILABLE, pageable);
        return videosPage.map(VideoResponse::fromEntity);
    }

    public VideoResponse getAvailableVideoById(Long videoId) {
        Video video = videoRepository.findByIdAndStatus(videoId, VIDEO_STATUS_AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId + " (available)"));
        return VideoResponse.fromEntity(video);
    }
    
    public Video getVideoEntityById(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
    }

    public Page<VideoResponse> getAvailableVideosByGenre(String genre, Pageable pageable) {
        Page<Video> videosPage = videoRepository.findByGenreAndStatus(genre, VIDEO_STATUS_AVAILABLE, pageable);
        return videosPage.map(VideoResponse::fromEntity);
    }

    public Page<VideoResponse> getAvailableVideosByTag(String tagName, Pageable pageable) {
        Page<Video> videosPage = videoRepository.findByTagNameAndStatus(tagName, VIDEO_STATUS_AVAILABLE, pageable);
        return videosPage.map(VideoResponse::fromEntity);
    }
    
    public List<String> getDistinctAvailableGenres() {
        return videoRepository.findDistinctGenres();
    }

    public Page<VideoResponse> searchAvailableVideosByTitle(String title, Pageable pageable) {
        Page<Video> videosPage = videoRepository.findByTitleContainingIgnoreCaseAndStatus(title, VIDEO_STATUS_AVAILABLE, pageable);
        return videosPage.map(VideoResponse::fromEntity);
    }

    // --- Delete Operations ---
    // ... (your existing deleteVideo method is fine here) ...
    @Transactional
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        if (video.getStorageObjectKey() != null && !video.getStorageObjectKey().isEmpty()) {
            fileStorageService.deleteFile(video.getStorageObjectKey());
        } 
        videoRepository.delete(video);
    }

    // --- Increment View Count ---
    // ... (your existing incrementViewCount method is fine here) ...
    @Transactional
    public void incrementViewCount(Long videoId) {
        Video video = videoRepository.findById(videoId)
            .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
    }

    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    //          ADD THIS NEW METHOD FOR STREAMING
    // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
    public ResponseEntity<Resource> streamVideo(Long videoId) {
        Video video = videoRepository.findByIdAndStatus(videoId, VIDEO_STATUS_AVAILABLE)
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId + " (not available for streaming)"));

        try {
            // Ensure storageObjectKey is not null or empty
            if (video.getStorageObjectKey() == null || video.getStorageObjectKey().trim().isEmpty()) {
                throw new ResourceNotFoundException("Video File Key", "for video id", videoId + " (key is missing)");
            }

            Path filePath = this.videoStorageLocation.resolve(video.getStorageObjectKey()).normalize();
            Resource resource = new FileSystemResource(filePath);

            if (resource.exists() && resource.isReadable()) {
                String contentType = "video/mp4"; // Default
                String filename = video.getStorageObjectKey().toLowerCase();

                if (filename.endsWith(".webm")) {
                    contentType = "video/webm";
                } else if (filename.endsWith(".ogv")) {
                    contentType = "video/ogg";
                } else if (filename.endsWith(".mov")) {
                    contentType = "video/quicktime";
                }
                // Add more common video types if needed

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // Log which file was not found for easier debugging
                System.err.println("Video file not found or not readable at path: " + filePath.toString());
                throw new ResourceNotFoundException("Video File", "path", filePath.toString());
            }
        } catch (ResourceNotFoundException rnfe) {
            // Re-throw specific ResourceNotFoundException
            throw rnfe;
        }
        catch (Exception ex) {
            // Log the exception for server-side diagnostics
            System.err.println("Error streaming video " + videoId + ": " + ex.getMessage());
            ex.printStackTrace(); // For more details during development
            throw new RuntimeException("Error streaming video: " + ex.getMessage(), ex);
        }
    }
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //          END OF ADDED STREAMING METHOD
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // Optional: Helper for ownership/admin check
    // ... (your existing checkOwnershipOrAdmin method is fine here) ...
    private void checkOwnershipOrAdmin(Long ownerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !userDetails.getId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("User does not have permission to modify this resource.");
        }
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')") // Secure at service layer too for defense in depth
    public Page<VideoResponse> getAllVideosForAdmin(Pageable pageable) {
        // No status filter here, fetches all videos
        Page<Video> videosPage = videoRepository.findAll(pageable);
        return videosPage.map(video -> VideoResponse.fromEntity(video)); // Map to DTO
    }
}