// src/components/Header.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Adjust path if AuthContext is elsewhere

const Header = () => {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setIsUserMenuOpen(false); // Close menu on logout
    navigate('/landing');
  };

  const toggleUserMenu = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
  };

  return (
    <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
      <nav className="container mx-auto px-4 sm:px-6 py-3 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-red-600">
          CloudCineStream
        </Link>

        <div className="flex items-center space-x-3 sm:space-x-4">
          {/* Search Bar Placeholder - We can integrate this later */}
          {/* <div className="hidden sm:block">
            <input type="search" placeholder="Search..." className="px-3 py-1.5 text-sm bg-gray-700 text-white rounded-md focus:outline-none focus:ring-1 focus:ring-netflix-red" />
          </div> */}

          {currentUser ? (
            <>
              {/* Admin Panel Link */}
              {currentUser.roles && currentUser.roles.includes('ROLE_ADMIN') && (
                <Link
                  to="/admin/dashboard" 
                  className="hidden sm:inline-block text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition-colors"
                >
                  Admin Panel
                </Link>
              )}

              {/* User Menu Dropdown */}
              <div className="relative">
                <button
                  onClick={toggleUserMenu}
                  className="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
                  id="user-menu-button"
                  aria-expanded={isUserMenuOpen}
                  aria-haspopup="true"
                >
                  <span className="sr-only">Open user menu</span>
                  {/* Placeholder for Avatar - replace with actual avatar later */}
                  <div className="h-8 w-8 rounded-full bg-netflix-red flex items-center justify-center text-white font-semibold">
                    {currentUser.firstName ? currentUser.firstName.charAt(0).toUpperCase() : (currentUser.email ? currentUser.email.charAt(0).toUpperCase() : 'U')}
                  </div>
                  <span className="hidden md:block ml-2 text-gray-300 hover:text-white">
                    {currentUser.firstName || (currentUser.email ? currentUser.email.split('@')[0] : 'User')}
                  </span>
                </button>

                {isUserMenuOpen && (
                  <div
                    className="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-gray-800 ring-1 ring-black ring-opacity-5 focus:outline-none"
                    role="menu"
                    aria-orientation="vertical"
                    aria-labelledby="user-menu-button"
                  >
                    <Link
                      to="/history"
                      className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-700 hover:text-white"
                      role="menuitem"
                      onClick={() => setIsUserMenuOpen(false)}
                    >
                      My Watch History
                    </Link>
                    <Link
                      to="/subscribe" // Link to PaymentPage
                      className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-700 hover:text-white"
                      role="menuitem"
                      onClick={() => setIsUserMenuOpen(false)}
                    >
                      Subscription
                    </Link>
                    <Link
                      to="/account" // Placeholder for account page
                      className="block px-4 py-2 text-sm text-gray-200 hover:bg-gray-700 hover:text-white"
                      role="menuitem"
                      onClick={() => setIsUserMenuOpen(false)}
                    >
                      My Account
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left px-4 py-2 text-sm text-gray-200 hover:bg-gray-700 hover:text-white"
                      role="menuitem"
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            // Login/Signup links if no user
            <div className="space-x-2 sm:space-x-3">
              <Link to="/login" className="text-gray-300 hover:text-white px-3 py-2 rounded-md text-sm font-medium">
                Sign In
              </Link>
              <Link
                to="/register"
                className="bg-netflix-red hover:bg-red-700 text-white px-3 sm:px-4 py-1.5 sm:py-2 rounded-md text-sm font-medium transition-colors"
              >
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
};

export default Header;