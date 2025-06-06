// src/services/admin.service.js
import apiClient from './api'; // Your configured Axios instance

const API_ADMIN_USERS_URL = "/admin/users"; // Base for admin user operations

/**
 * Fetches all users for admin view (paginated).
 */
const getAllUsers = (page = 0, size = 10, sort = 'createdAt,asc') => {
  return apiClient.get(`${API_ADMIN_USERS_URL}`, {
    params: { page, size, sort }
  });
};

/**
 * Fetches detailed information for a specific user by ID (admin view).
 * @param {string|number} userId
 */
const getUserById = (userId) => {
  return apiClient.get(`${API_ADMIN_USERS_URL}/${userId}`);
};

/**
 * Updates the roles for a specific user.
 * @param {string|number} userId
 * @param {string[]} roles - Array of role strings (e.g., ["ROLE_USER", "ROLE_UPLOADER"])
 */
const updateUserRoles = (userId, roles) => {
  return apiClient.put(`${API_ADMIN_USERS_URL}/${userId}/roles`, { roles });
};

/**
 * Updates the active status of a specific user.
 * @param {string|number} userId
 * @param {boolean} isActive
 */
const updateUserActiveStatus = (userId, isActive) => {
  return apiClient.patch(`${API_ADMIN_USERS_URL}/${userId}/status`, { active: isActive });
};

// Optional: Admin delete user (if you implement the backend)
// const deleteUser = (userId) => {
//   return apiClient.delete(`${API_ADMIN_USERS_URL}/${userId}`);
// };

const AdminService = {
  getAllUsers,
  getUserById,
  updateUserRoles,
  updateUserActiveStatus,
  // deleteUser,
};

export default AdminService;