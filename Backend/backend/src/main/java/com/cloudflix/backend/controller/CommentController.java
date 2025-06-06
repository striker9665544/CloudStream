// src/main/java/com/cloudflix/backend/controller/CommentController.java
package com.cloudflix.backend.controller;

import com.cloudflix.backend.dto.request.CommentRequest;
import com.cloudflix.backend.dto.response.CommentResponse;
import com.cloudflix.backend.dto.response.MessageResponse;
import com.cloudflix.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Or configure globally
@RestController
@RequestMapping("/api") // Base path, specific paths defined on methods
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Create a new top-level comment or a reply to a comment for a specific video
    @PostMapping("/videos/{videoId}/comments")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can comment
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long videoId,
            @Valid @RequestBody CommentRequest commentRequest) {
        CommentResponse createdComment = commentService.createComment(videoId, commentRequest);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    // Get all top-level comments for a specific video (paginated)
    // Replies might be included partially or fetched separately
    @GetMapping("/videos/{videoId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsForVideo(
            @PathVariable Long videoId,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) { // Default sort: newest first
        Page<CommentResponse> commentsPage = commentService.getTopLevelCommentsByVideo(videoId, pageable);
        return ResponseEntity.ok(commentsPage);
    }

    // Get all replies for a specific parent comment (paginated)
    @GetMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<List<CommentResponse>> getRepliesForComment( // Consider Page<CommentResponse> if pagination is critical
            @PathVariable Long parentCommentId,
            @PageableDefault(size = 5, sort = "createdAt,asc") Pageable pageable) { // Default sort: oldest first for replies
        List<CommentResponse> replies = commentService.getRepliesForComment(parentCommentId, pageable);
        return ResponseEntity.ok(replies);
    }

    // Update an existing comment
    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") // Further auth (ownership) is handled in the service
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        // parentCommentId in request body will be ignored for updates by current service logic
        CommentResponse updatedComment = commentService.updateComment(commentId, commentRequest);
        return ResponseEntity.ok(updatedComment);
    }

    // Delete a comment
    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") // Further auth (ownership or admin) is handled in the service
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully."));
    }

    // --- Optional: Admin/Moderator Endpoints for Comments ---
    /*
    @PatchMapping("/admin/comments/{commentId}/hide")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<CommentResponse> hideComment(@PathVariable Long commentId, @RequestParam boolean hide) {
        CommentResponse comment = commentService.hideComment(commentId, hide);
        return ResponseEntity.ok(comment);
    }
    */
}