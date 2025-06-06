// src/services/rating.service.js
import apiClient from './api';

const API_BASE_URL = ""; // Root is /api, specific paths defined below

// Add or update a rating for a specific video
const addOrUpdateRating = (videoId, ratingData) => {
  // ratingData should be an object like { ratingValue: number (1-5) }
  return apiClient.put(`${API_BASE_URL}/videos/${videoId}/ratings`, ratingData);
};

// Get the current authenticated user's rating for a specific video
const getUserRatingForVideo = (videoId) => {
  return apiClient.get(`${API_BASE_URL}/videos/${videoId}/ratings/my-rating`);
};

// Get the average rating and count for a specific video
const getVideoRatingSummary = (videoId) => {
  return apiClient.get(`${API_BASE_URL}/videos/${videoId}/ratings/summary`);
};

// Delete the current authenticated user's rating for a specific video
const deleteUserRatingForVideo = (videoId) => {
  return apiClient.delete(`${API_BASE_URL}/videos/${videoId}/ratings`);
};

const RatingService = {
  addOrUpdateRating,
  getUserRatingForVideo,
  getVideoRatingSummary,
  deleteUserRatingForVideo,
};

export default RatingService;