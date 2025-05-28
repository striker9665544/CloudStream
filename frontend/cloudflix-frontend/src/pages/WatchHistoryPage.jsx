// src/pages/WatchHistoryPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import HistoryService from '../services/history.service';
import VideoCard from '../components/VideoCard'; // Assuming VideoCard can display video summary
import VideoRowSkeleton from '../components/skeletons/VideoRowSkeleton'; // For loading state
import { useAuth } from '../contexts/AuthContext'; // To ensure user is logged in

const WatchHistoryPage = () => {
  const [historyItems, setHistoryItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);
  const { currentUser } = useAuth();

  const fetchHistory = useCallback(async (pageNum, initialLoad = false) => {
    if (!currentUser) {
      setError("Please log in to view your watch history.");
      setLoading(false);
      return;
    }
    if (initialLoad) setLoading(true);
    else setLoadingMore(true);

    setError('');
    try {
      const response = await HistoryService.getUserWatchHistory(pageNum, 12); // Fetch 12 items per page
      if (pageNum === 0) {
        setHistoryItems(response.data.content);
      } else {
        setHistoryItems(prevItems => [...prevItems, ...response.data.content]);
      }
      setTotalPages(response.data.totalPages);
    } catch (err) {
      console.error("Failed to fetch watch history:", err);
      setError(err.response?.data?.message || err.message || 'Could not load watch history.');
    }
    if (initialLoad) setLoading(false);
    else setLoadingMore(false);
  }, [currentUser]);

  useEffect(() => {
    fetchHistory(0, true); // Initial fetch for page 0
  }, [fetchHistory]); // fetchHistory is memoized by useCallback and depends on currentUser

  const loadMoreItems = () => {
    if (page < totalPages - 1 && !loadingMore) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchHistory(nextPage);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 text-white">
        <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
          <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
            <Link to="/" className="text-2xl font-bold text-red-600">CloudCineStream</Link>
            {/* Placeholder for user info or login button if needed */}
          </nav>
        </header>
        <main className="container mx-auto px-6 py-8">
          <h1 className="text-3xl font-semibold mb-6">My Watch History</h1>
          <VideoRowSkeleton numberOfRows={2} cardsPerRow={6} />
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex flex-col justify-center items-center p-4">
        <h1 className="text-3xl font-semibold mb-6">My Watch History</h1>
        <p className="text-red-500 text-xl">{error}</p>
        <Link to="/" className="mt-4 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">
          Go to Home
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <header className="bg-black bg-opacity-80 shadow-md sticky top-0 z-50 backdrop-blur-md">
        <nav className="container mx-auto px-6 py-3 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold text-red-600">CloudCineStream</Link>
          {/* You might want to include the same user welcome/logout from HomePage here */}
        </nav>
      </header>
      <main className="container mx-auto px-6 py-8">
        <h1 className="text-3xl font-semibold mb-8">My Watch History</h1>
        
        {historyItems.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 sm:gap-6">
            {historyItems.map(item => (
              // The VideoCard component expects a 'video' prop which should match VideoSummaryResponse structure
              <VideoCard key={item.watchHistoryId} video={item.video} />
            ))}
          </div>
        ) : (
          <p className="text-center text-gray-400 text-lg">You haven't watched any videos yet.</p>
        )}

        {loadingMore && <p className="text-center py-6 text-gray-400">Loading more...</p>}
        
        {!loading && !loadingMore && page < totalPages - 1 && historyItems.length > 0 && (
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

export default WatchHistoryPage;