// src/pages/GenrePage.jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import VideoService from '../services/video.service';
import VideoCard from '../components/VideoCard';
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton'; // Or a more specific page skeleton

const GenrePage = () => {
  const { genreSlug } = useParams();
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Convert slug back to title for display (simple version, might need more robust unslugify)
  const genreTitle = genreSlug.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

  useEffect(() => {
    const fetchVideosByGenre = async () => {
      setLoading(true);
      try {
        // Note: Backend expects actual genre name, not slug, if your service is set up that way
        // You might need to fetch all genres first, find the one matching the slug, then use its real name.
        // For simplicity now, assuming genreTitle from slug might work if backend is flexible or you adjust service.
        // A better way: pass actual genre name if known, or have an endpoint /api/videos/slug/{genreSlug}
        const response = await VideoService.getVideosByGenre(genreTitle, page, 24); // Fetch 24 per page
        setVideos(prevVideos => page === 0 ? response.data.content : [...prevVideos, ...response.data.content]);
        setTotalPages(response.data.totalPages);
      } catch (err) {
        setError('Failed to load videos for this genre.');
        console.error(err);
      }
      setLoading(false);
    };
    fetchVideosByGenre();
  }, [genreSlug, genreTitle, page]); // Re-fetch if slug or page changes

  const loadMore = () => {
    if (page < totalPages - 1) {
      setPage(prevPage => prevPage + 1);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* You'd have your main site header here */}
      <main className="container mx-auto px-6 py-8">
        <h1 className="text-3xl font-semibold mb-6">Genre: {genreTitle}</h1>
        {loading && page === 0 && <VideoRowSkeleton numberOfRows={2} cardsPerRow={6} />}
        {error && <p className="text-red-500">{error}</p>}
        
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
          {videos.map(video => (
            <VideoCard key={video.id} video={video} />
          ))}
        </div>

        {loading && page > 0 && <p className="text-center py-4">Loading more...</p>}
        {!loading && page < totalPages - 1 && videos.length > 0 && (
          <div className="text-center mt-8">
            <button
              onClick={loadMore}
              className="bg-red-600 hover:bg-red-700 text-white px-6 py-2 rounded-md"
            >
              Load More
            </button>
          </div>
        )}
        {!loading && videos.length === 0 && !error && <p>No videos found in this genre.</p>}
      </main>
    </div>
  );
};

export default GenrePage;