// src/services/video.service.js
import apiClient from './api'; // Your configured Axios instance

const API_VIDEO_URL = "/videos"; // Relative to baseURL in apiClient

// Fetch all available videos (paginated)
const getAllAvailableVideos = (page = 0, size = 20, sort = 'uploadTimestamp,desc') => {
  return apiClient.get(`${API_VIDEO_URL}`, {
    params: { page, size, sort }
  });
};

// Fetch a specific video by its ID
const getVideoById = (videoId) => {
  return apiClient.get(`${API_VIDEO_URL}/${videoId}`);
  // Note: The backend already increments view count on this call.
  // If you wanted to separate view recording, you'd call recordView separately.
};

// Fetch videos by genre (paginated)
const getVideosByGenre = (genreName, page = 0, size = 20) => {
  return apiClient.get(`${API_VIDEO_URL}/genre/${genreName}`, {
    params: { page, size }
  });
};

// Fetch videos by tag (paginated)
const getVideosByTag = (tagName, page = 0, size = 20) => {
  return apiClient.get(`${API_VIDEO_URL}/tag/${tagName}`, {
    params: { page, size }
  });
};

// Fetch distinct available genres
const getDistinctGenres = () => {
  return apiClient.get(`${API_VIDEO_URL}/genres`);
};

// Search videos by title (paginated)
const searchVideosByTitle = (title, page = 0, size = 20) => {
  return apiClient.get(`${API_VIDEO_URL}/search`, {
    params: { title, page, size }
  });
};

// --- Admin/Uploader specific functions (if needed from frontend later) ---
// const createVideoMetadata = (videoData) => {
//   return apiClient.post(API_VIDEO_URL, videoData);
// };

// const updateVideoMetadata = (videoId, videoData) => {
//   return apiClient.put(`${API_VIDEO_URL}/${videoId}`, videoData);
// };

// const updateVideoStatus = (videoId, status) => {
//   return apiClient.patch(`${API_VIDEO_URL}/${videoId}/status`, null, { params: { status } });
// };

// const deleteVideo = (videoId) => {
//   return apiClient.delete(`${API_VIDEO_URL}/${videoId}`);
// };

const VideoService = {
  getAllAvailableVideos,
  getVideoById,
  getVideosByGenre,
  getVideosByTag,
  getDistinctGenres,
  searchVideosByTitle,
  // createVideoMetadata, // Uncomment if/when admin panel is built
  // updateVideoMetadata,
  // updateVideoStatus,
  // deleteVideo,
};

export default VideoService;