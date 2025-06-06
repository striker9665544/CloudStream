// src/pages/admin/AdminDashboardPage.jsx
import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext'; // Adjust path as needed
import Header from '../../components/Header'; // Assuming you have a shared Header

const AdminDashboardPage = () => {
  const { currentUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Client-side check for admin role, backend @PreAuthorize is the source of truth
    if (currentUser && (!currentUser.roles || !currentUser.roles.includes('ROLE_ADMIN'))) {
      console.warn("Non-admin user attempting to access admin dashboard. Redirecting.");
      navigate('/'); // Redirect non-admins to home page
    }
  }, [currentUser, navigate]);

  // Optional: Add a loading state if fetching dashboard summary data in the future
  // if (!currentUser || !currentUser.roles.includes('ROLE_ADMIN')) {
  //   return <div className="min-h-screen bg-gray-900 flex justify-center items-center text-white"><p>Access Denied. Redirecting...</p></div>;
  // }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header />
      <main className="container mx-auto px-4 sm:px-6 py-8">
        <h1 className="text-3xl sm:text-4xl font-bold mb-8 text-center">Admin Dashboard</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Card for Upload Video */}
          <Link to="/admin/upload-video" className="block p-6 bg-gray-800 hover:bg-gray-700 rounded-lg shadow-lg transition-all duration-200 ease-in-out transform hover:scale-105">
            <h2 className="text-xl font-semibold mb-2 text-netflix-red">Upload Video</h2>
            <p className="text-sm text-gray-400">Add new video content to the platform.</p>
          </Link>

          {/* Card for Manage Videos */}
          <Link to="/admin/manage-videos" className="block p-6 bg-gray-800 hover:bg-gray-700 rounded-lg shadow-lg transition-all duration-200 ease-in-out transform hover:scale-105">
            <h2 className="text-xl font-semibold mb-2 text-netflix-red">Manage Videos</h2>
            <p className="text-sm text-gray-400">Edit metadata, change status, or delete existing videos.</p>
          </Link>

          {/* Card for Manage Users */}
          <Link to="/admin/manage-users" className="block p-6 bg-gray-800 hover:bg-gray-700 rounded-lg shadow-lg transition-all duration-200 ease-in-out transform hover:scale-105">
            <h2 className="text-xl font-semibold mb-2 text-netflix-red">Manage Users</h2>
            <p className="text-sm text-gray-400">View user list, update roles, and manage user status.</p>
          </Link>

          {/* Add more cards for other admin functionalities later */}
          {/* e.g., Site Statistics, Comment Moderation */}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboardPage;