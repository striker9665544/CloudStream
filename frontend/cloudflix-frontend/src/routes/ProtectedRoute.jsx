// src/routes/ProtectedRoute.jsx
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { currentUser, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    // You can replace this with a more sophisticated loading spinner component
    return <div className="min-h-screen flex items-center justify-center bg-gray-900 text-white text-xl">Loading application...</div>;
  }

  if (!currentUser) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If currentUser exists, they have an accessToken and user info
  return children ? children : <Outlet />;
};

export default ProtectedRoute;