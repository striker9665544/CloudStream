//src/services/history.service.js
import apiClient from './api'; // Your configured Axios instance

const API_HISTORY_URL = "/history"; // Relative to baseURL in apiClient

/**
 * Records or updates watch progress for a specific video.
 * @param {string|number} videoId - The ID of the video.
 * @param {object} progressData - The progress data.
 * @param {number} progressData.resumePositionSeconds - Current playback time in seconds.
 * @param {boolean} progressData.completed - Whether the video is completed.
 * @returns {Promise<AxiosResponse<any>>}
 */
const recordOrUpdateProgress = (videoId, progressData) => {
  return apiClient.put(`${API_HISTORY_URL}/video/${videoId}`, progressData);
};

/**
 * Fetches the watch history for the current authenticated user.
 * @param {number} [page=0] - The page number to fetch.
 * @param {number} [size=20] - The number of items per page.
 * @param {string} [sort='watchedAt,desc'] - The sorting criteria.
 * @returns {Promise<AxiosResponse<any>>}
 */
const getUserWatchHistory = (page = 0, size = 12, sort = 'watchedAt,desc') => {
  return apiClient.get(`${API_HISTORY_URL}/user`, {
    params: { page, size, sort }
  });
};

/**
 * Fetches the watch progress (resume time, completed status) for a specific video.
 * @param {string|number} videoId - The ID of the video.
 * @returns {Promise<AxiosResponse<any>>}
 */
const getWatchProgressForVideo = (videoId) => {
  return apiClient.get(`${API_HISTORY_URL}/video/${videoId}/progress`);
};

/**
 * Marks a video as fully watched for the current user.
 * @param {string|number} videoId - The ID of the video.
 * @returns {Promise<AxiosResponse<any>>}
 */
const markVideoAsCompleted = (videoId) => {
  return apiClient.post(`${API_HISTORY_URL}/video/${videoId}/complete`);
};

/**
 * Deletes a specific watch history entry by its ID.
 * @param {string|number} watchHistoryId - The ID of the watch history entry.
 * @returns {Promise<AxiosResponse<any>>}
 */
const deleteWatchHistoryEntry = (watchHistoryId) => {
  return apiClient.delete(`${API_HISTORY_URL}/${watchHistoryId}`);
};

/**
 * Clears all watch history for the current authenticated user.
 * @returns {Promise<AxiosResponse<any>>}
 */
const clearUserWatchHistory = () => {
  return apiClient.delete(`${API_HISTORY_URL}/user/clear`);
};


const HistoryService = {
  recordOrUpdateProgress,
  getUserWatchHistory,
  getWatchProgressForVideo,
  markVideoAsCompleted,       // Optional, include if you built this endpoint
  deleteWatchHistoryEntry,  // Optional
  clearUserWatchHistory     // Optional
};

export default HistoryService;