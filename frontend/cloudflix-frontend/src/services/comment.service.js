// src/services/comment.service.js
import apiClient from './api';

const API_BASE_URL = ""; // Root is /api, specific paths defined below

// Create a new comment (top-level or reply) for a video
const createComment = (videoId, commentData) => {
  // commentData should be an object like { text: "string", parentCommentId: number | null }
  return apiClient.post(`${API_BASE_URL}/videos/${videoId}/comments`, commentData);
};

// Get top-level comments for a video (paginated)
const getCommentsForVideo = (videoId, page = 0, size = 10, sort = 'createdAt,desc') => {
  return apiClient.get(`${API_BASE_URL}/videos/${videoId}/comments`, {
    params: { page, size, sort }
  });
};

// Get replies for a specific parent comment (paginated)
const getRepliesForComment = (parentCommentId, page = 0, size = 5, sort = 'createdAt,asc') => {
  return apiClient.get(`${API_BASE_URL}/comments/${parentCommentId}/replies`, {
    params: { page, size, sort }
  });
};

// Update an existing comment
const updateComment = (commentId, commentData) => {
  // commentData should be an object like { text: "new string" }
  return apiClient.put(`${API_BASE_URL}/comments/${commentId}`, commentData);
};

// Delete a comment
const deleteComment = (commentId) => {
  return apiClient.delete(`${API_BASE_URL}/comments/${commentId}`);
};

const CommentService = {
  createComment,
  getCommentsForVideo,
  getRepliesForComment,
  updateComment,
  deleteComment,
};

export default CommentService;