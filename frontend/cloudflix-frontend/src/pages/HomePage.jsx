//src/pages/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import VideoService from '../services/video.service';
import VideoRow from '../components/VideoRow';
import Banner from '../components/Banner';
import BannerSkeleton from '../components/skeletons/BannerSkeleton';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton';
import { PREDEFINED_GENRE_ROWS, slugify } from '../config/genres'; // Ensure slugify is here or imported correctly

const HomePage = () => {
  const { currentUser } = useAuth();
  const [featuredVideo, setFeaturedVideo] = useState(null);
  const [allFetchedRows, setAllFetchedRows] = useState([]); // Store all fetched rows
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeGenreFilter, setActiveGenreFilter] = useState(null); // To track which genre tab is active

  useEffect(() => {
    const fetchHomePageData = async () => {
      setLoading(true);
      setError('');
      try {
        // Fetch videos for each predefined genre category
        const rowsDataPromises = PREDEFINED_GENRE_ROWS.map(async (rowConfig) => {
          try {
            // Use getVideosByGenre and use rowConfig.tag AS THE GENRE NAME
            // Assuming rowConfig.tag stores the actual genre name like "Hollywood", "SciFi"
            const videosResponse = await VideoService.getVideosByGenre(rowConfig.tag, 0, 10);
            return {
              title: rowConfig.title,
              videos: videosResponse.data.content,
              genreName: rowConfig.tag, // Store the original genre name for filtering
              genreSlug: rowConfig.slug
            };
          } catch (genreFetchError) {
            console.error(`Error fetching videos for genre ${rowConfig.tag} (row: ${rowConfig.title}):`, genreFetchError);
            return { title: rowConfig.title, videos: [], genreName: rowConfig.tag, genreSlug: rowConfig.slug };
          }
        });

        let fetchedRows = await Promise.all(rowsDataPromises);
        // We might not want to filter out empty rows here if we want the tab to still exist
        // fetchedRows = fetchedRows.filter(row => row.videos && row.videos.length > 0);
        setAllFetchedRows(fetchedRows); // Store all fetched rows

        // Determine featured video from the first populated row among all fetched
        const firstPopulatedRow = fetchedRows.find(row => row.videos && row.videos.length > 0);
        if (firstPopulatedRow && firstPopulatedRow.videos.length > 0) {
          setFeaturedVideo(firstPopulatedRow.videos[0]);
        } else {
          // Fallback if no predefined rows have videos
          try {
            const recentVideosResponse = await VideoService.getAllAvailableVideos(0, 1);
            if (recentVideosResponse.data.content && recentVideosResponse.data.content.length > 0) {
              setFeaturedVideo(recentVideosResponse.data.content[0]);
            }
          } catch (fallbackError) {
            console.error("Error fetching fallback featured video:", fallbackError);
          }
        }
      } catch (err) {
        console.error("Failed to fetch home page data:", err);
        setError(err.response?.data?.message || err.message || 'Could not load videos.');
      }
      setLoading(false);
    };

    if (currentUser) {
      fetchHomePageData();
    } else {
      setLoading(false);
      setAllFetchedRows([]);
      setFeaturedVideo(null);
    }
  }, [currentUser]);

  // Filter videoRows based on activeGenreFilter for display
  const displayedVideoRows = activeGenreFilter
    ? allFetchedRows.filter(row => row.genreName === activeGenreFilter)
    : allFetchedRows; // Show all fetched rows if no filter is active

  if (loading) {
    // ... (loading JSX remains the same)
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        <Header />
        <BannerSkeleton />
        <main className="container mx-auto px-6 py-8">
          <VideoRowSkeleton numberOfRows={PREDEFINED_GENRE_ROWS.length || 3} cardsPerRow={5} />
        </main>
      </div>
    );
  }

  if (error) {
    // ... (error JSX remains the same)
    return (
        <div className="min-h-screen bg-gray-900 text-white">
            <Header />
            <div className="flex flex-col justify-center items-center pt-20">
                <p className="text-red-500 text-xl">{error}</p>
                <button onClick={() => window.location.reload()} className="mt-4 bg-blue-500 px-4 py-2 rounded">Try Again</button>
            </div>
        </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header />
      <Banner video={featuredVideo} />

      <main className="container mx-auto px-6 py-8">
        {/* Genre Tabs/Buttons */}
        <div className="mb-6 sm:mb-8 flex flex-wrap items-center justify-center gap-2 sm:gap-3">
          <button
            onClick={() => setActiveGenreFilter(null)} // Show all rows
            className={`px-3 py-1.5 sm:px-4 sm:py-2 rounded-full text-xs sm:text-sm font-medium transition-colors
                        ${!activeGenreFilter ? 'bg-netflix-red text-white' : 'bg-gray-700 hover:bg-gray-600 text-gray-300'}`}
          >
            All Curated
          </button>
          {PREDEFINED_GENRE_ROWS.map(genreConfig => (
            <button
              key={genreConfig.slug}
              onClick={() => setActiveGenreFilter(genreConfig.tag)} // Filter by the genre name (which is in rowConfig.tag)
              className={`px-3 py-1.5 sm:px-4 sm:py-2 rounded-full text-xs sm:text-sm font-medium transition-colors
                          ${activeGenreFilter === genreConfig.tag ? 'bg-netflix-red text-white' : 'bg-gray-700 hover:bg-gray-600 text-gray-300'}`}
            >
              {genreConfig.title}
            </button>
          ))}
        </div>

        {/* Display Video Rows */}
        {displayedVideoRows.length > 0 ? (
          displayedVideoRows.map((row, index) => (
            <VideoRow key={`${row.genreSlug}-${index}`} title={row.title} videos={row.videos} genreSlug={row.genreSlug} />
          ))
        ) : (
          !loading && activeGenreFilter && <p className="text-center text-gray-400 text-lg">No videos found for "{activeGenreFilter}".</p>
        )}
        {!loading && allFetchedRows.length === 0 && !activeGenreFilter && (
          <p className="text-center text-gray-400 text-lg">No curated videos available at the moment.</p>
        )}
      </main>
    </div>
  );
};

export default HomePage;

// src/pages/HomePage.jsx
/*import React, { useState, useEffect } from 'react';
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
            {/* Header content can be simplified or shown during loading *///}
         /*</nav>
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
              {/* Consider a User Dropdown Menu Here *///}
              /*<Link to="/history" className="text-gray-300 hover:text-white text-sm">My History</Link>
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

export default HomePage;*/


