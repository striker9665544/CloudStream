// src/main/java/com/cloudflix/backend/repository/CommentRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.Comment;
import com.cloudflix.backend.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds all top-level comments (those without a parent) for a given video,
     * ordered by creation date (newest first or oldest first, depending on preference).
     * Replies would typically be fetched lazily or explicitly when expanding a comment.
     *
     * @param video The video for which to fetch comments.
     * @param pageable Pagination information.
     * @return A page of top-level comments.
     */
    Page<Comment> findByVideoAndParentCommentIsNullOrderByCreatedAtDesc(Video video, Pageable pageable);
    // Or use OrderByCreatedAtAsc for oldest first

    /**
     * Finds all replies for a given parent comment.
     * This can be useful if you want to load replies separately on demand.
     *
     * @param parentComment The parent comment for which to fetch replies.
     * @return A list of reply comments, typically ordered by creation date.
     */
    List<Comment> findAllByParentCommentOrderByCreatedAtAsc(Comment parentComment);


    // Optional: If you want to fetch comments and their first level of replies eagerly in one go
    // This can be more complex and might lead to N+1 issues if not handled carefully.
    // For simpler scenarios, fetching top-level comments and then fetching replies on demand is often preferred.
    /*
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.replies r " +
           "WHERE c.video = :video AND c.parentComment IS NULL " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsWithRepliesByVideo(@Param("video") Video video, Pageable pageable);
    */

    // Count comments for a video (could be useful for display)
    long countByVideoAndParentCommentIsNull(Video video); // Count only top-level comments
    long countByVideo(Video video); // Count all comments including replies for a video
}