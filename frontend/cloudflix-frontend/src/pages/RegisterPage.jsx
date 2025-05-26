// src/pages/RegisterPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    middleName: '', // Optional
    email: '',
    password: '',
    dateOfBirth: '', // Format: YYYY-MM-DD
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password) {
        setError('Please fill in all required fields.');
        setLoading(false);
        return;
    }
    if (!/\S+@\S+\.\S+/.test(formData.email)) {
        setError('Please enter a valid email address.');
        setLoading(false);
        return;
    }
    if (formData.password.length < 6) {
         setError('Password must be at least 6 characters long.');
         setLoading(false);
         return;
    }

    try {
      await register(
        formData.firstName,
        formData.lastName,
        formData.email,
        formData.password,
        formData.dateOfBirth || null,
        formData.middleName || null
      );
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to register. Please try again.');
    }
    setLoading(false);
  };

  return (
    // 1. Outermost div for full-page background and flex centering
    <div className="login-page-bg min-h-screen flex items-center justify-center text-white p-4">
      {/* 2. Full-screen semi-transparent overlay */}
      <div className="absolute inset-0 bg-black opacity-60"></div>

      {/* 3. Form Card - positioned above the overlay */}
      <div className="relative z-10 bg-black bg-opacity-75 p-8 sm:p-12 rounded-md shadow-xl w-full max-w-lg"> {/* max-w-lg for wider form */}
        
        <div className="flex justify-start mb-8"> {/* Or justify-center if you prefer */}
          <img src="/netflix-logo.png" alt="CloudFlix Logo" className="h-8 sm:h-10" />
        </div>

        <h2 className="text-3xl font-bold mb-6 text-white">Create Account</h2> {/* Heading inside the card */}

        {error && <p className="bg-red-700 text-white p-3 rounded mb-4 text-sm text-center">{error}</p>} {/* Error message inside the card */}
        {success && <p className="bg-green-600 text-white p-3 rounded mb-4 text-sm text-center">{success}</p>} {/* Success message inside the card */}

        <form onSubmit={handleSubmit} className="space-y-4"> {/* Form also inside the card */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-gray-300 mb-1">First Name*</label>
              <input type="text" name="firstName" id="firstName" value={formData.firstName} onChange={handleChange} required className="w-full input-style" placeholder="John"/>
            </div>
            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-gray-300 mb-1">Last Name*</label>
              <input type="text" name="lastName" id="lastName" value={formData.lastName} onChange={handleChange} required className="w-full input-style" placeholder="Doe"/>
            </div>
          </div>
          <div>
            <label htmlFor="middleName" className="block text-sm font-medium text-gray-300 mb-1">Middle Name (Optional)</label>
            <input type="text" name="middleName" id="middleName" value={formData.middleName} onChange={handleChange} className="w-full input-style" placeholder="M."/>
          </div>
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-300 mb-1">Email*</label>
            <input type="email" name="email" id="email" value={formData.email} onChange={handleChange} required className="w-full input-style" placeholder="you@example.com"/>
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-1">Password*</label>
            <input type="password" name="password" id="password" value={formData.password} onChange={handleChange} required className="w-full input-style" placeholder="••••••••"/>
          </div>
          <div>
            <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-300 mb-1">Date of Birth (Optional)</label>
            <input type="date" name="dateOfBirth" id="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} className="w-full input-style text-gray-400" />
          </div>
          {/* The <style jsx> tag should be outside the form, but still within the RegisterPage component's return if you keep it local */}
          <button type="submit" disabled={loading} className="w-full bg-netflix-red hover:bg-red-700 text-white font-semibold py-3 px-4 rounded-md transition duration-200 ease-in-out disabled:opacity-60 disabled:cursor-not-allowed">
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>

        <p className="mt-8 text-center text-sm text-gray-400"> {/* Link to login inside the card */}
          Already have an account? <Link to="/login" className="text-white hover:underline font-semibold">Sign in.</Link>
        </p>
      </div>
      {/* Moved <style jsx> outside the main content flow of the card, but still within the component's return block */}
      <style jsx>{`
         .input-style {
             background-color: #374151; /* bg-gray-700 */
             border: 1px solid #4B5563; /* border-gray-600 */
             border-radius: 0.375rem; /* rounded-md */
             padding: 0.75rem 1rem; /* px-4 py-3 */
             color: white;
         }
         .input-style:focus {
             outline: none;
             box-shadow: 0 0 0 2px #EF4444; /* focus:ring-2 focus:ring-red-500 */
         }
         .input-style::placeholder {
             color: #6B7280; /* placeholder-gray-500 */
         }
      `}</style>
    </div>
  );
};

export default RegisterPage;