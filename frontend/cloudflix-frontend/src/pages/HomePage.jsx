// src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext'; // Assuming path is correct
import VideoService from '../services/video.service';
import VideoRow from '../components/VideoRow';
import Banner from '../components/Banner';
import BannerSkeleton from '../components/skeletons/BannerSkeleton';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton';
import { PREDEFINED_GENRE_ROWS } from '../config/genres'; // <<< IMPORT PREDEFINED GENRES

const HomePage = () => {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const [featuredVideo, setFeaturedVideo] = useState(null);
  const [videoRows, setVideoRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchHomePageData = async () => {
      setLoading(true);
      setError('');
      try {
        // Use PREDEFINED_GENRE_ROWS to fetch specific categories
        const rowsDataPromises = PREDEFINED_GENRE_ROWS.map(async (rowConfig) => {
          try {
            // Fetch videos by the 'tag' specified in rowConfig
            const videosResponse = await VideoService.getVideosByTag(rowConfig.tag, 0, 10); // Fetch 10 videos per row
            return {
              title: rowConfig.title,
              videos: videosResponse.data.content,
              genreSlug: rowConfig.slug // Use the slug from config for "See All" link
            };
          } catch (tagError) {
            console.error(`Error fetching videos for tag ${rowConfig.tag} (row: ${rowConfig.title}):`, tagError);
            return { title: rowConfig.title, videos: [], genreSlug: rowConfig.slug }; // Return empty for this row on error
          }
        });

        let fetchedRows = await Promise.all(rowsDataPromises);
        // Optionally filter out rows that ended up with no videos, or display them with a "No videos" message
        fetchedRows = fetchedRows.filter(row => row.videos && row.videos.length > 0);
        setVideoRows(fetchedRows);

        // Determine featured video (e.g., from the first video of the first populated row)
        if (fetchedRows.length > 0 && fetchedRows[0].videos.length > 0) {
          setFeaturedVideo(fetchedRows[0].videos[0]);
        } else {
          // Fallback if no predefined rows have videos: fetch some recent videos
          try {
            const recentVideosResponse = await VideoService.getAllAvailableVideos(0, 1); // Fetch 1 most recent
            if (recentVideosResponse.data.content && recentVideosResponse.data.content.length > 0) {
              setFeaturedVideo(recentVideosResponse.data.content[0]);
            }
          } catch (fallbackError) {
            console.error("Error fetching fallback featured video:", fallbackError);
          }
        }

      } catch (err) { // Catch errors from Promise.all or initial setup
        console.error("Failed to fetch home page data:", err);
        // Check if the error is from getDistinctGenres if you were still calling that before
        setError(err.response?.data?.message || err.message || 'Could not load videos.');
      }
      setLoading(false);
    };

    // Decide when to fetch data (e.g., if user is logged in, or if home page is public)
    if (currentUser) { // Example: Only fetch if user is logged in
      fetchHomePageData();
    } else {
      // If homepage can be viewed by non-logged-in users, you might fetch data here too,
      // or set videoRows to an empty array or some public content.
      setLoading(false); // Set loading to false if no data is fetched for non-logged-in users
      setVideoRows([]);
      setFeaturedVideo(null);
    }
  }, [currentUser]); // Re-fetch if currentUser changes

  const handleLogout = () => {
    logout();
    navigate('/landing');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
          <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
            <Link to="/" className="text-2xl font-bold text-red-600">CloudCineStream</Link>
            {/* Header content can be simplified or shown during loading */}
          </nav>
        </header>
        <BannerSkeleton />
        <main className="container mx-auto px-6 py-8">
          <VideoRowSkeleton numberOfRows={PREDEFINED_GENRE_ROWS.length || 3} cardsPerRow={5} />
        </main>
      </div>
    );
  }

  // ... (rest of your error and main return JSX for HomePage remains largely the same)
  // Ensure the header part in the main return also handles the currentUser logic for welcome/logout or login/signup
  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
        <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold text-red-600">
            CloudCineStream
          </Link>
          {currentUser && (
            <div className="flex items-center space-x-4">
              <span className="text-gray-300">
                Welcome, {currentUser.firstName || (currentUser.email ? currentUser.email.split('@')[0] : 'User')}!
              </span>
              {/* Consider a User Dropdown Menu Here */}
              <Link to="/history" className="text-gray-300 hover:text-white text-sm">My History</Link>
              {currentUser.roles.includes('ROLE_ADMIN') && (
                <Link to="/admin/upload-video" className="text-gray-300 hover:text-white text-sm">Upload Video</Link>
                // Add more admin links here or a link to an /admin dashboard
              )}
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          )}
          {!currentUser && !loading && (
             <div className="space-x-3">
                <Link to="/login" className="text-gray-300 hover:text-white">Sign In</Link>
                <Link to="/register" className="bg-red-600 hover:bg-red-700 text-white px-3 py-1.5 rounded-md text-sm">Sign Up</Link>
             </div>
          )}
        </nav>
      </header>

      <Banner video={featuredVideo} />

      <main className="container mx-auto px-6 py-8">
        {videoRows.length > 0 ? (
          videoRows.map((row, index) => (
            <VideoRow key={index} title={row.title} videos={row.videos} genreSlug={row.genreSlug} />
          ))
        ) : (
          !loading && <p className="text-center text-gray-400 text-lg">No videos available for these categories yet.</p>
        )}
      </main>
    </div>
  );
};

export default HomePage;