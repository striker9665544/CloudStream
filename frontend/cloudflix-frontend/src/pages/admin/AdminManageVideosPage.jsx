// src/pages/admin/AdminManageVideosPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import VideoService from '../../services/video.service';
import { useAuth } from '../../contexts/AuthContext';
import Header from '../../components/Header'; // Assuming shared Header
import { formatDate } from '../../utils/dateFormatter'; // Assuming you create this utility

// You might want a date formatter utility
// Create src/utils/dateFormatter.js
// export const formatDate = (dateString) => {
//   if (!dateString) return 'N/A';
//   try {
//     return new Date(dateString).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
//   } catch (e) { return 'Invalid Date'; }
// };
// export const formatDateTime = (dateString) => { /* ... */ };

const AdminManageVideosPage = () => {
  const [videos, setVideos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [refreshTrigger, setRefreshTrigger] = useState(0); // To trigger re-fetch

  const { currentUser } = useAuth();
  const navigate = useNavigate();
  

  useEffect(() => {
    if (currentUser && (!currentUser.roles || !currentUser.roles.includes('ROLE_ADMIN'))) {
      navigate('/');
    }
  }, [currentUser, navigate]);

  const fetchAdminVideos = useCallback(async (pageNum) => {
    setLoading(true);
    setError('');
    try {
      const response = await VideoService.adminGetAllVideos(pageNum, 10); // 10 videos per page
      setVideos(response.data.content);
      setTotalPages(response.data.totalPages);
      setPage(response.data.number); // Spring Page is 0-indexed
    } catch (err) {
      console.error("Failed to fetch admin videos:", err);
      setError(err.response?.data?.message || "Could not load videos.");
    }
    setLoading(false);
  }, []); // Depends on nothing that changes frequently from parent

  useEffect(() => {
    if (currentUser && currentUser.roles && currentUser.roles.includes('ROLE_ADMIN')) {
        fetchAdminVideos(page);
    }
  }, [currentUser, page, fetchAdminVideos, refreshTrigger]);

  const handleStatusChange = async (videoId, newStatus) => {
    if (!window.confirm(`Are you sure you want to change status to ${newStatus}?`)) return;
    try {
      await VideoService.adminUpdateVideoStatus(videoId, newStatus);
      setRefreshTrigger(prev => prev + 1); // Trigger re-fetch
      alert("Status updated successfully!");
    } catch (err) {
      console.error("Failed to update status:", err);
      alert("Failed to update status: " + (err.response?.data?.message || err.message));
    }
  };

  const handleDeleteVideo = async (videoId, videoTitle) => {
    if (!window.confirm(`Are you sure you want to delete video: "${videoTitle}" (ID: ${videoId})? This action cannot be undone.`)) return;
    try {
      await VideoService.adminDeleteVideo(videoId);
      setRefreshTrigger(prev => prev + 1); // Trigger re-fetch
      alert("Video deleted successfully!");
    } catch (err) {
      console.error("Failed to delete video:", err);
      alert("Failed to delete video: " + (err.response?.data?.message || err.message));
    }
  };
  
  // Placeholder for Edit - navigates to a new page or opens a modal
  const handleEditVideo = (videoId) => {
    // For now, let's imagine a simple alert. Later this would navigate or open modal.
    // navigate(`/admin/edit-video/${videoId}`);
    alert(`Edit video ID: ${videoId} - Feature to be implemented.`);
    // TODO: Implement navigation to an edit page or modal
  };

  const renderPagination = () => {
    if (totalPages <= 1) return null;
    const pageNumbers = [];
    // Logic to create a limited number of page buttons (e.g., first, last, current +/- 2)
    // For simplicity, showing all if not too many, or a simpler prev/next logic
    let startPage = Math.max(0, page - 2);
    let endPage = Math.min(totalPages - 1, page + 2);

    if (page < 2) endPage = Math.min(totalPages -1, 4);
    if (page > totalPages - 3) startPage = Math.max(0, totalPages - 5);


    if (startPage > 0) {
        pageNumbers.push(<button key="first" onClick={() => setPage(0)} className="px-3 py-1 mx-1 rounded bg-gray-600 hover:bg-gray-500">1</button>);
        if (startPage > 1) pageNumbers.push(<span key="start-ellipsis" className="px-3 py-1">...</span>);
    }

    for (let i = startPage; i <= endPage; i++) {
      pageNumbers.push(
        <button
          key={i}
          onClick={() => setPage(i)}
          disabled={loading || i === page}
          className={`px-3 py-1 mx-1 rounded ${i === page ? 'bg-netflix-red text-white' : 'bg-gray-600 hover:bg-gray-500'}`}
        >
          {i + 1}
        </button>
      );
    }
    
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) pageNumbers.push(<span key="end-ellipsis" className="px-3 py-1">...</span>);
        pageNumbers.push(<button key="last" onClick={() => setPage(totalPages - 1)} className="px-3 py-1 mx-1 rounded bg-gray-600 hover:bg-gray-500">{totalPages}</button>);
    }


    return (
      <div className="mt-6 flex justify-center items-center">
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={loading || page === 0} className="px-3 py-1 mx-1 rounded bg-gray-600 hover:bg-gray-500 disabled:opacity-50">Prev</button>
        {pageNumbers}
        <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={loading || page === totalPages - 1} className="px-3 py-1 mx-1 rounded bg-gray-600 hover:bg-gray-500 disabled:opacity-50">Next</button>
      </div>
    );
  };


  if (!currentUser || !currentUser.roles || !currentUser.roles.includes('ROLE_ADMIN')) {
    // This will be caught by useEffect redirect, but good for initial render check
    return <div className="min-h-screen bg-gray-900 flex justify-center items-center text-white"><p>Access Denied.</p></div>;
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      <Header />
      <main className="container mx-auto px-4 sm:px-6 py-8">
        <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl sm:text-3xl font-bold">Manage Videos</h1>
            <Link to="/admin/upload-video" className="bg-netflix-red hover:bg-red-700 text-white font-semibold px-4 py-2 rounded-md text-sm">
                + Upload New Video
            </Link>
        </div>

        {loading && <p className="text-center text-gray-400">Loading videos...</p>}
        {error && <p className="text-center text-red-500 bg-red-900 border border-red-700 p-3 rounded-md">{error}</p>}

        {!loading && !error && videos.length === 0 && (
          <p className="text-center text-gray-400">No videos found.</p>
        )}

        {!loading && !error && videos.length > 0 && (
          <div className="overflow-x-auto bg-gray-800 shadow-md rounded-lg">
            <table className="min-w-full divide-y divide-gray-700">
              <thead className="bg-gray-700">
                <tr>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">ID</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Thumbnail</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Title</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Status</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Uploaded</th>
                  <th scope="col" className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-gray-800 divide-y divide-gray-700">
                {videos.map((video) => (
                  <tr key={video.id} className="hover:bg-gray-700/50">
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{video.id}</td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <img src={video.thumbnailUrl || `https://via.placeholder.com/100x56.png?text=${video.title.substring(0,10)}`} alt={video.title} className="w-20 h-auto object-cover rounded" />
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-white">{video.title}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm">
                      <select
                        value={video.status}
                        onChange={(e) => handleStatusChange(video.id, e.target.value)}
                        className={`p-1 rounded text-xs ${
                            video.status === 'AVAILABLE' ? 'bg-green-700 text-green-100' :
                            video.status === 'PENDING_PROCESSING' ? 'bg-yellow-600 text-yellow-100' :
                            'bg-red-700 text-red-100'
                        } border-transparent focus:ring-0 focus:border-transparent`}
                        style={{minWidth: '120px'}}
                      >
                        <option value="PENDING_PROCESSING">PENDING</option>
                        <option value="AVAILABLE">AVAILABLE</option>
                        <option value="UNAVAILABLE">UNAVAILABLE</option>
                        {/* Add more statuses if your backend supports them */}
                      </select>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">{formatDate(video.uploadTimestamp)}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm font-medium space-x-2">
                      <button onClick={() => handleEditVideo(video.id)} className="text-blue-400 hover:text-blue-300">Edit</button>
                      <button onClick={() => handleDeleteVideo(video.id, video.title)} className="text-red-400 hover:text-red-300">Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && renderPagination()}
      </main>
    </div>
  );
};

export default AdminManageVideosPage;