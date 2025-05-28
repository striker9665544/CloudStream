// src/services/api.js
import axios from 'axios';
import AuthService from './auth.service'; // Assuming auth.service.js is in the same /services folder

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const instance = axios.create({
  baseURL: API_BASE_URL,
  //headers: {
  //  "Content-Type": "application/json",
  //},
});

instance.interceptors.request.use(
  (config) => {
    const user = AuthService.getCurrentUser();
    if (user && user.accessToken) { // Check for accessToken
      config.headers["Authorization"] = 'Bearer ' + user.accessToken;
    }
    if (!(config.data instanceof FormData)) {
      if (!config.headers['Content-Type']) { 
         config.headers['Content-Type'] = 'application/json';
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      AuthService.logout();
      // Consider a more robust way to trigger redirect, e.g., via a custom event or context update
      // window.location.href = '/login'; // Can be problematic
      console.error("401 Unauthorized from API - logging out user.");
    }
    return Promise.reject(error);
  }
);

export default instance; // <<< Ensure it has a default export
