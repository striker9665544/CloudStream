// src/main/java/com/cloudflix/backend/service/CommentService.java
package com.cloudflix.backend.service;

import com.cloudflix.backend.dto.request.CommentRequest;
import com.cloudflix.backend.dto.response.CommentResponse;
import com.cloudflix.backend.entity.Comment;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.exception.ResourceNotFoundException;
import com.cloudflix.backend.repository.CommentRepository;
import com.cloudflix.backend.repository.UserRepository;
import com.cloudflix.backend.repository.VideoRepository;
import com.cloudflix.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final int MAX_INITIAL_REPLIES_TO_INCLUDE = 3; // Configurable: How many replies to send with parent

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User must be authenticated to perform this action.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
    }

    @Transactional
    public CommentResponse createComment(Long videoId, CommentRequest commentRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        Video video = videoRepository.findByIdAndStatus(videoId, "AVAILABLE") // Only comment on available videos
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setUser(currentUser);
        comment.setVideo(video);

        if (commentRequest.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Comment", "id", commentRequest.getParentCommentId()));
            // Ensure parent comment belongs to the same video
            if (!parentComment.getVideo().getId().equals(videoId)) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified video.");
            }
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.fromEntity(savedComment, false, 0); // Don't include replies for a newly created comment
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getTopLevelCommentsByVideo(Long videoId, Pageable pageable) {
        Video video = videoRepository.findByIdAndStatus(videoId, "AVAILABLE")
                .orElseThrow(() -> new ResourceNotFoundException("Video", "id", videoId));

        Page<Comment> commentsPage = commentRepository.findByVideoAndParentCommentIsNullOrderByCreatedAtDesc(video, pageable);
        return commentsPage.map(comment -> CommentResponse.fromEntity(comment, true, MAX_INITIAL_REPLIES_TO_INCLUDE));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesForComment(Long parentCommentId, Pageable pageable) {
        Comment parentComment = commentRepository.findById(parentCommentId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent Comment", "id", parentCommentId));

        // Using Pageable for replies, although typically reply lists might not be huge per parent.
        // If you expect very deep threads or many replies, Pageable is good.
        // For simplicity, repository method returns List, but could return Page.
        // Here, we'll just fetch all replies as per repository method, sorting is handled by repo.
        // If Page<Comment> was returned by repo: parentComment.getReplies() might also work if collection is EAGER or session open.
        // But explicit query is safer.
        List<Comment> replies = commentRepository.findAllByParentCommentOrderByCreatedAtAsc(parentComment);
        // To apply Pageable if findAllByParentCommentOrderByCreatedAtAsc accepted it:
        // Page<Comment> repliesPage = commentRepository.findAllByParentCommentOrderByCreatedAtAsc(parentComment, pageable);
        // return repliesPage.map(reply -> CommentResponse.fromEntity(reply, false, 0)).getContent();

        return replies.stream()
                      .map(reply -> CommentResponse.fromEntity(reply, false, 0)) // Replies of replies not included by default
                      .collect(Collectors.toList());
    }


    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Authorization: Only the author of the comment can update it
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this comment.");
        }

        // Only text can be updated. Parent cannot be changed once set.
        comment.setText(commentRequest.getText());
        // comment.setUpdatedAt will be handled by @UpdateTimestamp

        Comment updatedComment = commentRepository.save(comment);
        // Fetch replies if necessary for the response, or keep it simple
        return CommentResponse.fromEntity(updatedComment, true, MAX_INITIAL_REPLIES_TO_INCLUDE);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User currentUser = getCurrentAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Authorization: Author or Admin can delete
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_ADMIN"));
        if (!comment.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this comment.");
        }

        // If deleting a parent comment, its replies will also be deleted due to cascade or orphanRemoval.
        // If orphanRemoval=true is set on Comment.replies, child replies are removed when parent link is broken.
        // If CascadeType.ALL includes CascadeType.REMOVE, deleting parent also deletes children.
        commentRepository.delete(comment);
    }

    // --- Moderation methods (Admin only - can be added later) ---
    /*
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')") // Assuming a MODERATOR role
    public CommentResponse hideComment(Long commentId, boolean hide) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        comment.setHidden(hide);
        comment.setModerated(true); // Mark as moderated
        Comment updatedComment = commentRepository.save(comment);
        return CommentResponse.fromEntity(updatedComment);
    }
    */
}