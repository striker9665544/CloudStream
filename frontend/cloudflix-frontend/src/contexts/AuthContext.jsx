// src/contexts/AuthContext.jsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import AuthService from '../services/auth.service';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(AuthService.getCurrentUser());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const userData = await AuthService.login(email, password);
      setCurrentUser(userData); // userData contains accessToken, id, email, etc.
      return userData;
    } catch (error) {
      console.error("Login failed in AuthContext:", error);
      throw error; // Re-throw to be caught by the component
    }
  };

  const register = async (firstName, lastName, email, password, dateOfBirth, middleName = null) => {
    try {
      // AuthService.register now handles the optional middleName
      const response = await AuthService.register(firstName, lastName, email, password, dateOfBirth, middleName);
      return response; // This is the backend's MessageResponse
    } catch (error) {
      console.error("Registration failed in AuthContext:", error);
      throw error;
    }
  };

  const logout = () => {
    AuthService.logout();
    setCurrentUser(null);
  };

  const value = {
    currentUser, // This will now hold the object with accessToken, email, roles, etc.
    loading,
    login,
    register,
    logout,
  };

  // Only render children when not loading to prevent flicker or premature access
  return <AuthContext.Provider value={value}>{!loading && children}</AuthContext.Provider>;
};

export const useAuth = () => {
  return useContext(AuthContext);
};