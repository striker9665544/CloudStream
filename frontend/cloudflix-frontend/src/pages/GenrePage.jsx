//src/pages/GenrePage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import VideoService from '../services/video.service';
import VideoCard from '../components/VideoCard';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton';
import { PREDEFINED_GENRE_ROWS } from '../config/genres'; // Your genre configuration
import Header from '../components/Header'; // Assuming you want to use the shared Header

const GenrePage = () => {
  const { genreSlug } = useParams(); // e.g., "hollywood", "sci-fi"
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true); // For initial load of the genre
  const [error, setError] = useState('');
  const [page, setPage] = useState(0); // Current page for pagination
  const [totalPages, setTotalPages] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false); // For "Load More" button

  // Find the genre configuration based on the slug
  // The `tag` property in PREDEFINED_GENRE_ROWS now represents the actual genre name for the API
  const genreConfig = PREDEFINED_GENRE_ROWS.find(g => g.slug === genreSlug);

  // Determine display title and the genre name to use for API calls
  // Fallback if slug isn't in our predefined list (e.g., user types a URL manually)
  const genreDisplayTitle = genreConfig
    ? genreConfig.title
    : genreSlug.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

  const genreApiName = genreConfig // This is the name to pass to getVideosByGenre
    ? genreConfig.tag // Use the 'tag' field from config as the actual genre name
    : genreSlug.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());


  const fetchVideosBySpecificGenre = useCallback(async (pageNum, initialLoad = false) => {
    if (initialLoad) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }
    setError('');

    try {
      // *** CRITICAL CHANGE: Use getVideosByGenre ***
      const response = await VideoService.getVideosByGenre(genreApiName, pageNum, 24); // Fetch 24 per page

      if (pageNum === 0) {
        setVideos(response.data.content);
      } else {
        setVideos(prevVideos => [...prevVideos, ...response.data.content]);
      }
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError(`Failed to load videos for ${genreDisplayTitle}.`);
      console.error(`Error fetching videos for genre "${genreApiName}":`, err);
      // Keep existing videos on error if loading more, clear if initial load error
      if (pageNum === 0) setVideos([]);
    } finally {
      if (initialLoad) {
        setLoading(false);
      } else {
        setLoadingMore(false);
      }
    }
  }, [genreApiName, genreDisplayTitle]); // Dependencies for useCallback

  // Effect for initial load or when genreApiName (derived from genreSlug) changes
  useEffect(() => {
    setPage(0); // Reset page to 0
    setVideos([]); // Clear previous videos when genre changes
    if (genreApiName) { // Only fetch if we have a valid genre name
      fetchVideosBySpecificGenre(0, true); // Initial fetch for the new genre (page 0)
    } else {
      setError(`Genre configuration not found for slug: ${genreSlug}`);
      setLoading(false);
    }
  }, [genreApiName, fetchVideosBySpecificGenre, genreSlug]); // fetchVideosBySpecificGenre is now stable due to useCallback

  // Effect for fetching more pages when 'page' state changes (and is > 0)
  useEffect(() => {
    if (page > 0) {
      fetchVideosBySpecificGenre(page);
    }
  }, [page, fetchVideosBySpecificGenre]); // fetchVideosBySpecificGenre is now stable

  const loadMoreItems = () => {
    if (page < totalPages - 1 && !loadingMore && !loading) { // Ensure not already loading
      setPage(prevPage => prevPage + 1);
    }
  };

  if (loading && page === 0) { // Show skeleton only on initial page load
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        <Header />
        <main className="container mx-auto px-6 py-8">
          <h1 className="text-3xl font-semibold mb-6">Genre: {genreDisplayTitle}</h1>
          <VideoRowSkeleton numberOfRows={3} cardsPerRow={6} /> {/* More rows for a page view */}
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header /> {/* Use the shared Header component */}
      <main className="container mx-auto px-6 py-8">
        <h1 className="text-3xl font-semibold mb-8">Genre: {genreDisplayTitle}</h1>

        {error && <p className="text-red-500 text-center py-4">{error}</p>}

        {videos.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 sm:gap-6">
            {videos.map(video => (
              <VideoCard key={video.id} video={video} />
            ))}
          </div>
        ) : (
          !loading && !error && <p className="text-center text-gray-400 text-lg">No videos found in this genre.</p>
        )}

        {loadingMore && <p className="text-center py-6 text-gray-400">Loading more...</p>}

        {!loading && !loadingMore && page < totalPages - 1 && videos.length > 0 && (
          <div className="text-center mt-10">
            <button
              onClick={loadMoreItems}
              disabled={loadingMore}
              className="bg-red-600 hover:bg-red-700 text-white font-semibold py-2 px-6 rounded-md transition duration-200 ease-in-out disabled:opacity-60"
            >
              {loadingMore ? 'Loading...' : 'Load More'}
            </button>
          </div>
        )}
      </main>
    </div>
  );
};

export default GenrePage;