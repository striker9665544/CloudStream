// src/pages/GenrePage.jsx
import React, { useState, useEffect, useCallback } from 'react'; // Added useCallback
import { useParams, Link } from 'react-router-dom'; // Added Link for header
import VideoService from '../services/video.service';
import VideoCard from '../components/VideoCard';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton';
import { PREDEFINED_GENRE_ROWS } from '../config/genres'; // <<< IMPORT PREDEFINED GENRES

const GenrePage = () => {
  const { genreSlug } = useParams();
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);

  // Find the genre configuration based on the slug
  const genreConfig = PREDEFINED_GENRE_ROWS.find(g => g.slug === genreSlug);
  
  // Determine display title and API tag name
  // Fallback if slug isn't in our predefined list (e.g., user types a URL manually for a non-curated tag)
  const genreDisplayTitle = genreConfig ? genreConfig.title : genreSlug.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  const genreApiTag = genreConfig ? genreConfig.tag : genreSlug.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());


  const fetchVideos = useCallback(async (pageNum, initialLoad = false) => {
    if (initialLoad) setLoading(true);
    else setLoadingMore(true);
    setError('');

    try {
      // Use the resolved genreApiTag to fetch videos
      // Assuming your backend VideoService.getVideosByTag() uses the actual tag name
      const response = await VideoService.getVideosByTag(genreApiTag, pageNum, 24); // Fetch 24 per page
      
      if (pageNum === 0) {
        setVideos(response.data.content);
      } else {
        setVideos(prevVideos => [...prevVideos, ...response.data.content]);
      }
      setTotalPages(response.data.totalPages);
    } catch (err) {
      setError(`Failed to load videos for ${genreDisplayTitle}.`);
      console.error(`Error fetching videos for tag "${genreApiTag}":`, err);
    } finally {
      if (initialLoad) setLoading(false);
      else setLoadingMore(false);
    }
  }, [genreApiTag, genreDisplayTitle]); // Dependencies for useCallback

  useEffect(() => {
    setPage(0); // Reset page to 0 when genreSlug/genreApiTag changes
    setVideos([]); // Clear previous videos
    fetchVideos(0, true); // Initial fetch for the new genre
  }, [genreApiTag, fetchVideos]); // Re-fetch if genreApiTag changes

  useEffect(() => {
    if (page > 0) { // Only fetch for subsequent pages if page number changes and > 0
        fetchVideos(page);
    }
  }, [page, fetchVideos]);


  const loadMoreItems = () => {
    if (page < totalPages - 1 && !loadingMore) {
      setPage(prevPage => prevPage + 1);
      // The useEffect listening to 'page' will trigger the fetch
    }
  };
  
  // Basic header (can be replaced with your shared Header component)
  const renderHeader = () => (
    <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
      <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-red-600">CloudCineStream</Link>
        {/* Add user menu/login buttons if needed, similar to HomePage */}
      </nav>
    </header>
  );

  if (loading && page === 0) { // Show skeleton only on initial load of the page/genre
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        {renderHeader()}
        <main className="container mx-auto px-6 py-8">
          <h1 className="text-3xl font-semibold mb-6">Genre: {genreDisplayTitle}</h1>
          <VideoRowSkeleton numberOfRows={2} cardsPerRow={6} />
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {renderHeader()}
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