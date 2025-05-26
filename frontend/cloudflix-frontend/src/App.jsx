// src/App.jsx
import React from 'react';
import {
    BrowserRouter as Router,
    Routes,
    Route,
    // Link, // Only if used directly in App.jsx
    // useNavigate, // Only if used directly in App.jsx
    Navigate
} from 'react-router-dom';

import { AuthProvider } from './contexts/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProtectedRoute from './routes/ProtectedRoute';
import LandingPage from './pages/LandingPage';
import HomePage from './pages/HomePage';
import PaymentPage from './pages/PaymentPage'; // <<< ADD THIS IMPORT

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/landing" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/subscribe" element={<PaymentPage />} /> {/* This line was causing the error if PaymentPage was not imported */}
          </Route>

          <Route path="*" element={<Navigate to="/landing" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;