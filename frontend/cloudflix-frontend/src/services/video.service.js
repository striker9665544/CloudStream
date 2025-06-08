// src/services/video.service.js
import apiClient from './api'; // Your configured Axios instance

const API_ADMIN_VIDEO_URL = "/admin/videos";
const API_VIDEO_URL = "/videos"; // Relative to baseURL in apiClient
const API_UPLOAD_URL = "/upload"; // Added for clarity for upload endpoints

// Fetch all available videos (paginated)
const getAllAvailableVideos = (page = 0, size = 20, sort = 'uploadTimestamp,desc') => {
  return apiClient.get(`${API_VIDEO_URL}`, {
    params: { page, size, sort }
  });
};

const getSecureStreamUrl = (videoId) => {
  return apiClient.get(`${API_VIDEO_URL}/${videoId}/stream-url`);
};

// Fetch a specific video by its ID
const getVideoById = (videoId) => {
  return apiClient.get(`${API_VIDEO_URL}/${videoId}`);
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

// --- NEW Admin Specific Functions (DEFINED BEFORE VideoService OBJECT) ---
const adminGetAllVideos = (page = 0, size = 10, sort = 'uploadTimestamp,desc') => {
  return apiClient.get(`${API_ADMIN_VIDEO_URL}`, {
    params: { page, size, sort }
  });
};

/**
 * Admin updates the status of a specific video.
 * @param {string|number} videoId - The ID of the video.
 * @param {string} status - The new status (e.g., "AVAILABLE", "PENDING_PROCESSING", "UNAVAILABLE").
 */
const adminUpdateVideoStatus = (videoId, status) => {
  return apiClient.patch(`${API_ADMIN_VIDEO_URL}/${videoId}/status`, null, { // Body is null, status is query param
    params: { status }
  });
};


/**
 * Admin updates the metadata of any video.
 * Note: Backend endpoint might be the same as general video update if permissions allow.
 * Or could be a specific admin endpoint. Assuming PUT /api/admin/videos/{videoId} for this.
 * @param {string|number} videoId - The ID of the video.
 * @param {object} metadata - The metadata object.
 */
const adminUpdateVideoMetadata = (videoId, metadata) => {
  return apiClient.put(`${API_ADMIN_VIDEO_URL}/${videoId}`, metadata);
};

/**
 * Admin deletes any video.
 * @param {string|number} videoId - The ID of the video to delete.
 */
const adminDeleteVideo = (videoId) => {
  return apiClient.delete(`${API_ADMIN_VIDEO_URL}/${videoId}`);
};

/**
 * Uploads a video file along with its metadata.
 * @param {File} videoFile - The video file to upload.
 * @param {object} metadata - An object containing video metadata (title, description, etc.).
 * @param {function} [onUploadProgress] - Optional callback for upload progress.
 * @returns {Promise<AxiosResponse<any>>}
 */
const uploadVideoWithMetadata = (videoFile, metadata, onUploadProgress) => {
  const formData = new FormData();
  formData.append('videoFile', videoFile, videoFile.name); // Added filename for the file part, good practice

  // === MODIFICATION ===
  // Send metadata as a plain JSON string.
  // The browser will likely send this part with Content-Type: text/plain.
  // Spring MVC's @RequestPart can often handle this by using a
  // StringHttpMessageConverter first, then Jackson to parse the string into the DTO.
  formData.append('metadata', JSON.stringify(metadata));
  // === END MODIFICATION ===

  return apiClient.post(`${API_UPLOAD_URL}/video`, formData, {
    // Axios will automatically set the overall Content-Type to multipart/form-data
    // when FormData is used as the body.
    // No need to set overall 'Content-Type' in headers here.
    onUploadProgress: onUploadProgress
  });
};

const VideoService = {
  getAllAvailableVideos,
  getVideoById,
  getVideosByGenre,
  getVideosByTag,
  getDistinctGenres,
  searchVideosByTitle,
  uploadVideoWithMetadata,
  getSecureStreamUrl,

  
  // Admin functions
  adminGetAllVideos,
  adminUpdateVideoStatus,
  adminUpdateVideoMetadata,
  adminDeleteVideo,
};

export default VideoService;