// src/pages/HomePage.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Import Link and useNavigate
import { useAuth } from '../contexts/AuthContext';   // Import useAuth

const HomePage = () => {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/landing'); // Or to a public landing page like /login
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <header className="bg-black shadow-md sticky top-0 z-50">
        <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold text-red-600">
            CloudCineStream
          </Link>
          {currentUser && ( // Check if currentUser exists before trying to access its properties
            <div className="flex items-center space-x-4">
              <span className="text-gray-300">
                Welcome, {currentUser.firstName || (currentUser.email ? currentUser.email.split('@')[0] : 'User')}!
              </span>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          )}
        </nav>
      </header>
      <main className="container mx-auto px-6 py-8">
        <h2 className="text-3xl font-semibold mb-6">Browse</h2>
        <p className="mb-4">Your Netflix-style content (rows of movies/shows) will go here.</p>
        <p className="text-gray-400 text-sm">You are logged in. Your JWT Access Token is stored.</p>
        {currentUser && (
             <div className="mt-4 p-4 bg-gray-800 rounded shadow">
                 <h3 className="text-lg font-semibold mb-2">Current User Details (from localStorage):</h3>
                 <pre className="text-xs whitespace-pre-wrap break-all">{JSON.stringify(currentUser, null, 2)}</pre>
             </div>
        )}
      </main>
    </div>
  );
};

export default HomePage;