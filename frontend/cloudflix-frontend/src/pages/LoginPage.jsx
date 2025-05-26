// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to login. Please check your credentials.';
      setError(errorMessage);
    }
    setLoading(false);
  };

  return (
    <div className="login-page-bg min-h-screen flex items-center justify-center text-white p-4">
      {/* Full-screen overlay (optional, adjust opacity as needed) */}
      <div className="absolute inset-0 bg-black opacity-60"></div>

      {/* Form Card - positioned above the overlay */}
      <div className="relative z-10 bg-black bg-opacity-75 p-8 sm:p-12 rounded-md shadow-xl w-full max-w-md">
        <div className="flex justify-start mb-8"> {/* Changed to justify-start for Netflix style */}
          <img src="/logo512.png" alt="CloudFlix Logo" className="h-8 sm:h-10" /> {/* Adjust height */}
        </div>

        <h2 className="text-3xl font-bold mb-6 text-white">Sign In</h2>

        {error && <p className="bg-red-700 text-white p-3 rounded mb-4 text-sm">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            {/* <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-1">Email</label> */}
            <input
              type="email"
              id="email"
              name="email" // Good practice to add name attribute
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded focus:outline-none focus:ring-2 focus:ring-red-500 placeholder-gray-400"
              placeholder="Email or phone number" // Netflix style placeholder
            />
          </div>

          <div>
            {/* <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-1">Password</label> */}
            <input
              type="password"
              id="password"
              name="password" // Good practice
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded focus:outline-none focus:ring-2 focus:ring-red-500 placeholder-gray-400"
              placeholder="Password" // Netflix style placeholder
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-netflix-red hover:bg-red-700 text-white font-semibold py-3 px-4 rounded transition duration-200 ease-in-out disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>

        <div className="mt-8 text-sm">
            <p className="text-gray-400">
                New to CloudFlix? <Link to="/register" className="text-white hover:underline font-semibold">Sign up now.</Link>
            </p>
            <p className="mt-3 text-xs text-gray-500">
                This page is protected by Google reCAPTCHA to ensure you're not a bot. {/*<a href="#" className="text-blue-500 hover:underline">Learn more.</a>*/}
            </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;