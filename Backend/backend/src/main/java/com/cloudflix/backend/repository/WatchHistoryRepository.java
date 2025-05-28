//src/main/java/com/cloudflix/backend/repository/WatchHistoryRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import com.cloudflix.backend.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    // Find a specific watch history entry for a user and a video
    Optional<WatchHistory> findByUserAndVideo(User user, Video video);

    // Find all watch history entries for a user, ordered by most recently watched
    // This will be used for the user's "Watch History" page
    Page<WatchHistory> findAllByUserOrderByWatchedAtDesc(User user, Pageable pageable);

    // Check if a watch history entry exists for a user and video
    boolean existsByUserAndVideo(User user, Video video);

    // You might add more specific queries later if needed, for example:
    // Find all completed videos for a user
    // Page<WatchHistory> findByUserAndCompletedTrueOrderByWatchedAtDesc(User user, Pageable pageable);

    // Example: Count distinct videos watched by a user (could be useful for stats)
    // @Query("SELECT COUNT(DISTINCT wh.video) FROM WatchHistory wh WHERE wh.user = :user")
    // long countDistinctVideosByUser(@Param("user") User user);
}