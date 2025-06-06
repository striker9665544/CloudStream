// src/main/java/com/cloudflix/backend/dto/response/CommentResponse.java
package com.cloudflix.backend.dto.response;

import com.cloudflix.backend.entity.Comment;
import com.cloudflix.backend.entity.User; // Needed for UserInfo

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// No Lombok as per your preference for manual methods
public class CommentResponse {

    private Long id;
    private String text;
    private UserInfoResponse author; // Simplified user info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentCommentId; // ID of the parent, if this is a reply
    private int replyCount;       // Number of direct replies to this comment
    private List<CommentResponse> replies; // Optional: for a few initial replies

    // --- Constructors ---
    public CommentResponse() {
    }

    public CommentResponse(Long id, String text, UserInfoResponse author, LocalDateTime createdAt, LocalDateTime updatedAt, Long parentCommentId, int replyCount, List<CommentResponse> replies) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.parentCommentId = parentCommentId;
        this.replyCount = replyCount;
        this.replies = replies;
    }


    // Static factory method
    public static CommentResponse fromEntity(Comment comment, boolean includeReplies, int maxRepliesToInclude) {
        if (comment == null) {
            return null;
        }

        UserInfoResponse authorInfo = null;
        if (comment.getUser() != null) {
            authorInfo = new UserInfoResponse(
                    comment.getUser().getId(),
                    // Display name preference: firstName, or email prefix if firstName is null
                    comment.getUser().getFirstName() != null && !comment.getUser().getFirstName().isEmpty()
                            ? comment.getUser().getFirstName()
                            : (comment.getUser().getEmail() != null ? comment.getUser().getEmail().split("@")[0] : "User"),
                    null // Placeholder for avatar URL if you add it later
            );
        }

        Long parentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;
        
        List<CommentResponse> replyDtos = null;
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            replyDtos = comment.getReplies().stream()
                               .sorted((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt())) // Oldest replies first
                               .limit(maxRepliesToInclude) // Limit number of included replies
                               .map(reply -> CommentResponse.fromEntity(reply, false, 0)) // Don't include nested replies for initial load
                               .collect(Collectors.toList());
        }


        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                authorInfo,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                parentId,
                comment.getReplies() != null ? comment.getReplies().size() : 0, // Total direct reply count
                replyDtos // Included replies (subset or null)
        );
    }
    
    // Overloaded factory method for simplicity when replies are not needed or handled separately
    public static CommentResponse fromEntity(Comment comment) {
        return fromEntity(comment, false, 0); // By default, don't include replies deeply
    }


    // --- Getters ---
    public Long getId() { return id; }
    public String getText() { return text; }
    public UserInfoResponse getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getParentCommentId() { return parentCommentId; }
    public int getReplyCount() { return replyCount; }
    public List<CommentResponse> getReplies() { return replies; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setAuthor(UserInfoResponse author) { this.author = author; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public void setReplies(List<CommentResponse> replies) { this.replies = replies; }


    // --- Nested DTO for simplified User Information ---
    // (Could be a shared DTO if used elsewhere)
    public static class UserInfoResponse {
        private Long id;
        private String displayName;
        private String avatarUrl; // Placeholder for future

        public UserInfoResponse() {}

        public UserInfoResponse(Long id, String displayName, String avatarUrl) {
            this.id = id;
            this.displayName = displayName;
            this.avatarUrl = avatarUrl;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}