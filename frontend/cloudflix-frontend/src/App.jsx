// src/utils/App.jsx
import React from 'react';
import {
    BrowserRouter as Router,
    Routes,
    Route,
    Navigate
} from 'react-router-dom';

// Assuming AuthContext.jsx is in src/contexts/
import { AuthProvider } from './contexts/AuthContext'; 

// Assuming pages are in src/pages/
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
// Assuming ProtectedRoute is in src/routes/
import ProtectedRoute from './routes/ProtectedRoute';
import LandingPage from './pages/LandingPage';
import HomePage from './pages/HomePage';
import PaymentPage from './pages/PaymentPage';
import GenrePage from './pages/GenrePage'; // You added this
import PlayerPage from './pages/PlayerPage';

function App() {
  return (
    <AuthProvider> {/* <--- This is where the error occurs if AuthProvider is not defined */}
      <Router>
        <Routes>
          <Route path="/landing" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/subscribe" element={<PaymentPage />} />
            <Route path="/genre/:genreSlug" element={<GenrePage />} />
            <Route path="/player/:videoId" element={<PlayerPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/landing" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;