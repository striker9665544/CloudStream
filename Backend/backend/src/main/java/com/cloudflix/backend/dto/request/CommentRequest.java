// src/main/java/com/cloudflix/backend/dto/request/CommentRequest.java
package com.cloudflix.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Since you're doing manual methods, no Lombok annotations like @Data here
public class CommentRequest {

    @NotBlank(message = "Comment text cannot be blank.")
    @Size(min = 1, max = 5000, message = "Comment text must be between 1 and 5000 characters.")
    private String text;

    // Optional: For creating a reply. If null, it's a top-level comment.
    private Long parentCommentId;

    // --- Constructors ---
    public CommentRequest() {
    }

    public CommentRequest(String text) {
        this.text = text;
    }

    public CommentRequest(String text, Long parentCommentId) {
        this.text = text;
        this.parentCommentId = parentCommentId;
    }

    // --- Getters ---
    public String getText() {
        return text;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    // --- Setters ---
    public void setText(String text) {
        this.text = text;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
}