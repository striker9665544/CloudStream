// src/services/auth.service.js
import axios from 'axios';

const API_URL = "http://localhost:8080/api/auth/"; // Your backend API URL

const register = (firstName, lastName, email, password, dateOfBirth, middleName = null) => {
  // Ensure middleName is handled, even if null
  const payload = {
    firstName,
    lastName,
    email,
    password,
    dateOfBirth,
  };
  if (middleName) {
    payload.middleName = middleName;
  }
  return axios.post(API_URL + "signup", payload);
};

const login = (email, password) => {
  return axios
    .post(API_URL + "signin", {
      email,
      password,
    })
    .then((response) => {
      // The backend now returns accessToken, id, email, firstName, roles
      // The 'type: "Bearer"' is implicit or part of the token prefix in usage.
      // Let's store the entire user object from the response.
      if (response.data.accessToken) { // Check for accessToken
        localStorage.setItem("user", JSON.stringify(response.data));
      }
      return response.data;
    });
};

const logout = () => {
  localStorage.removeItem("user");
  // You might want to call a backend /api/auth/logout endpoint in the future
  // if you implement server-side token invalidation (more advanced).
};

const getCurrentUser = () => {
  const userStr = localStorage.getItem("user");
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};

const AuthService = {
  register,
  login,
  logout,
  getCurrentUser,
};

export default AuthService;