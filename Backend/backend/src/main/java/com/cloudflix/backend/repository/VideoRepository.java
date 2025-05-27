// src/main/java/com/cloudflix/backend/repository/VideoRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Page<Video> findByStatus(String status, Pageable pageable);

    Page<Video> findByGenreAndStatus(String genre, String status, Pageable pageable);

    // Find by tag name (requires joining with the tags table)
    // Note: For complex queries like this, especially with multiple tags, consider Querydsl or Criteria API
    // This is a simplified version for finding videos associated with a single tag name.
    @Query("SELECT v FROM Video v JOIN v.tags t WHERE t.name = :tagName AND v.status = :status")
    Page<Video> findByTagNameAndStatus(@Param("tagName") String tagName, @Param("status") String status, Pageable pageable);

    // Optional: Search by title (case-insensitive)
    Page<Video> findByTitleContainingIgnoreCaseAndStatus(String title, String status, Pageable pageable);

    Optional<Video> findByIdAndStatus(Long id, String status);

    List<Video> findByUploaderIdAndStatus(Long uploaderId, String status, Pageable pageable);

    // A query to find distinct genres for category listing
    @Query("SELECT DISTINCT v.genre FROM Video v WHERE v.status = 'AVAILABLE' AND v.genre IS NOT NULL AND v.genre <> '' ORDER BY v.genre ASC")
    List<String> findDistinctGenres();
}