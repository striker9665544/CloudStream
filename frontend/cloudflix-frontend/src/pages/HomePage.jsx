// src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import VideoService from '../services/video.service';
import VideoRow from '../components/VideoRow';
import Banner from '../components/Banner'; // Assuming you moved Banner to its own file
import BannerSkeleton from '../components/skeletons/BannerSkeleton';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton';

// Helper to create a URL slug
const slugify = (text) => {
  if (!text) return '';
  return text.toString().toLowerCase()
    .replace(/\s+/g, '-')           // Replace spaces with -
    .replace(/[^\w-]+/g, '')       // Remove all non-word chars
    .replace(/--+/g, '-')           // Replace multiple - with single -
    .replace(/^-+/, '')             // Trim - from start of text
    .replace(/-+$/, '');            // Trim - from end of text
};

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
        const genresResponse = await VideoService.getDistinctGenres();
        const genres = genresResponse.data;

        const rowsDataPromises = genres.map(async (genre) => {
          try {
            const videosResponse = await VideoService.getVideosByGenre(genre, 0, 10); // Fetch more videos for carousel
            return {
              title: genre,
              videos: videosResponse.data.content,
              genreSlug: slugify(genre) // Add slug for "See All" link
            };
          } catch (genreError) {
            console.error(`Error fetching videos for genre ${genre}:`, genreError);
            return { title: genre, videos: [], genreSlug: slugify(genre) };
          }
        });

        let fetchedRows = await Promise.all(rowsDataPromises);
        fetchedRows = fetchedRows.filter(row => row.videos && row.videos.length > 0);
        setVideoRows(fetchedRows);

        if (fetchedRows.length > 0 && fetchedRows[0].videos.length > 0) {
          setFeaturedVideo(fetchedRows[0].videos[0]);
        } else {
          const recentVideosResponse = await VideoService.getAllAvailableVideos(0, 1);
          if (recentVideosResponse.data.content && recentVideosResponse.data.content.length > 0) {
            setFeaturedVideo(recentVideosResponse.data.content[0]);
          }
        }
      } catch (err) {
        console.error("Failed to fetch home page data:", err);
        setError(err.response?.data?.message || err.message || 'Could not load videos.');
      }
      setLoading(false);
    };

    if (currentUser) { // Only fetch if user is logged in; adjust if home page is public
        fetchHomePageData();
    } else {
        setLoading(false); // If no user and home is protected, perhaps redirect or show login prompt
    }
  }, [currentUser]); // Re-fetch if user changes (e.g., logs in)

  const handleLogout = () => {
    logout();
    navigate('/landing');
  };

  // Loading State UI
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        {/* Header can still be shown during loading if desired */}
        <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md"> {/* ... header JSX ... */} </header>
        <BannerSkeleton />
        <main className="container mx-auto px-6 py-8">
          <VideoRowSkeleton numberOfRows={3} cardsPerRow={5} />
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex flex-col justify-center items-center">
        <p className="text-red-500 text-xl">{error}</p>
        <button onClick={() => window.location.reload()} className="mt-4 bg-blue-500 px-4 py-2 rounded">Try Again</button>
      </div>
    );
  }

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
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Logout
              </button>
            </div>
          )}
          {!currentUser && !loading && ( // Show login/register if not loading and no user
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
          !loading && <p className="text-center text-gray-400 text-lg">No videos available at the moment. Check back soon!</p>
        )}
      </main>
    </div>
  );
};

export default HomePage;