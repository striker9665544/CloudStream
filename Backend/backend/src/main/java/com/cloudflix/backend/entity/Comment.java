// src/main/java/com/cloudflix/backend/entity/Comment.java
package com.cloudflix.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects; // For a potentially better equals/hashCode

@Entity
@Table(name = "comments") // Ensure this table exists as per your cloudflix_db.sql
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id") // Nullable for top-level comments
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> replies = new HashSet<>();

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT") // Ensure TEXT type for potentially long comments
    private String text;

    @Column(name = "is_moderated", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean moderated = false;

    @Column(name = "is_hidden", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean hidden = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public Comment() {
        // JPA a no-arg constructor
    }

    // Constructor for a new top-level comment
    public Comment(Video video, User user, String text) {
        this.video = video;
        this.user = user;
        this.text = text;
    }

    // Constructor for a new reply comment
    public Comment(Video video, User user, String text, Comment parentComment) {
        this.video = video;
        this.user = user;
        this.text = text;
        this.parentComment = parentComment;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }

    public Set<Comment> getReplies() { return replies; }
    public void setReplies(Set<Comment> replies) { this.replies = replies; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isModerated() { return moderated; }
    public void setModerated(boolean moderated) { this.moderated = moderated; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods for managing replies (bidirectional if needed by your logic)
    public void addReply(Comment reply) {
        if (this.replies == null) {
            this.replies = new HashSet<>();
        }
        this.replies.add(reply);
        reply.setParentComment(this);
    }

    public void removeReply(Comment reply) {
        if (this.replies != null) {
            this.replies.remove(reply);
            reply.setParentComment(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id != null && id.equals(comment.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}