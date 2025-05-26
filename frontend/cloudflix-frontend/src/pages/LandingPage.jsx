// src/pages/LandingPage.jsx
import React from 'react';
import { Link } from 'react-router-dom'; // If you use Link components

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center p-8">
      {/* You can place your logo here */}
      {/* <img src="/netflix_logo.svg" alt="CloudFlix" className="h-16 mb-8" /> */}
      <h1 className="text-5xl font-bold text-red-600 mb-6">Welcome to CloudFlix</h1>
      <p className="text-xl text-gray-300 mb-10 text-center max-w-2xl">
        Your ultimate destination for movies and TV shows. Sign in to continue or create an account.
      </p>
      <div className="space-x-4">
        <Link
          to="/login"
          className="bg-red-600 hover:bg-red-700 text-white text-lg font-semibold py-3 px-8 rounded-md transition duration-200"
        >
          Sign In
        </Link>
        <Link
          to="/register"
          className="border-2 border-red-600 text-red-500 hover:bg-red-600 hover:text-white text-lg font-semibold py-3 px-8 rounded-md transition duration-200"
        >
          Sign Up
        </Link>
      </div>
      <footer className="absolute bottom-8 text-gray-500 text-sm">
        © {new Date().getFullYear()} CloudFlix. All rights reserved. (A demo project)
      </footer>
    </div>
  );
};

export default LandingPage;