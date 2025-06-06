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
import WatchHistoryPage from './pages/WatchHistoryPage';
import UploadVideoPage from './pages/admin/UploadVideoPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminManageVideosPage from './pages/admin/AdminManageVideosPage';
import AdminManageUsersPage from './pages/admin/AdminManageUsersPage';

function App() {
  return (
    <AuthProvider> {/* <--- This is where the error occurs if AuthProvider is not defined */}
      <Router>
        <Routes>
          <Route path="/landing" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            {/* User-facing routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/subscribe" element={<PaymentPage />} />
            <Route path="/genre/:genreSlug" element={<GenrePage />} />
            <Route path="/player/:videoId" element={<PlayerPage />} />
            <Route path="/history" element={<WatchHistoryPage />} />
            
            {/* Admin-facing routes */}
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            <Route path="/admin/upload-video" element={<UploadVideoPage />} /> {/* KEEP ONLY ONE */}
            <Route path="/admin/manage-videos" element={<AdminManageVideosPage />} />
            {<Route path="/admin/manage-users" element={<AdminManageUsersPage />} />  }
          </Route>

          <Route path="*" element={<Navigate to="/landing" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;