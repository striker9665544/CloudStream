// src/main/java/com/cloudflix/backend/repository/RatingRepository.java
package com.cloudflix.backend.repository;

import com.cloudflix.backend.entity.Rating;
import com.cloudflix.backend.entity.User;
import com.cloudflix.backend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Finds a rating given by a specific user for a specific video.
     * Useful for checking if a user has already rated a video or for retrieving their existing rating.
     *
     * @param user The user who gave the rating.
     * @param video The video that was rated.
     * @return An Optional containing the Rating if found, otherwise empty.
     */
    Optional<Rating> findByUserAndVideo(User user, Video video);

    /**
     * Finds all ratings for a specific video.
     * Useful for calculating an average rating.
     *
     * @param video The video for which to fetch all ratings.
     * @return A list of all ratings for the given video.
     */
    List<Rating> findAllByVideo(Video video);

    /**
     * Calculates the average rating for a specific video.
     * This uses a JPQL query to perform the aggregation in the database.
     *
     * @param video The video for which to calculate the average rating.
     * @return The average rating value, or null if no ratings exist for the video.
     */
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.video = :video")
    Double findAverageRatingByVideo(@Param("video") Video video);

    /**
     * Counts the number of ratings for a specific video.
     * @param video The video for which to count ratings.
     * @return The total number of ratings.
     */
    long countByVideo(Video video);

    // You could also add methods to find all ratings by a specific user if needed for a "My Ratings" page:
    // List<Rating> findAllByUserOrderByUpdatedAtDesc(User user);
}