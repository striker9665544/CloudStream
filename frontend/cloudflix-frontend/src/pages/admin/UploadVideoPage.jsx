// src/pages/admin/UploadVideoPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import VideoService from '../../services/video.service'; // Adjust path if needed
import { useAuth } from '../../contexts/AuthContext';   // Adjust path if needed

const UploadVideoPage = () => {
  const [videoFile, setVideoFile] = useState(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [genre, setGenre] = useState('');
  const [tags, setTags] = useState(''); // Comma-separated string for now
  // Optional fields you might add later:
  // const [durationSeconds, setDurationSeconds] = useState('');
  // const [thumbnailUrl, setThumbnailUrl] = useState('');

  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const navigate = useNavigate();
  const { currentUser } = useAuth();

  // Redirect if not admin (basic check, backend is the source of truth for role)
  // A more robust way would be to have specific roles like ROLE_UPLOADER
  useEffect(() => {
    if (currentUser && !currentUser.roles.includes('ROLE_ADMIN')) {
      // console.warn("User is not an admin. Redirecting from upload page.");
      // navigate('/'); // Or to a "not authorized" page
    }
  }, [currentUser, navigate]);


  const handleFileChange = (event) => {
    setVideoFile(event.target.files[0]);
    setUploadProgress(0); // Reset progress when new file is selected
    setSuccessMessage('');
    setError('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!videoFile) {
      setError('Please select a video file to upload.');
      return;
    }
    if (!title.trim()) {
      setError('Please enter a title for the video.');
      return;
    }

    setIsUploading(true);
    setError('');
    setSuccessMessage('');
    setUploadProgress(0);

    const metadata = {
      title,
      description,
      genre,
      // durationSeconds: durationSeconds ? parseInt(durationSeconds) : null,
      // thumbnailUrl,
      tags: tags.split(',').map(tag => tag.trim()).filter(tag => tag !== ''), // Convert CSV string to array of strings
    };

    try {
      const response = await VideoService.uploadVideoWithMetadata(
        videoFile,
        metadata,
        (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percentCompleted);
        }
      );
      setSuccessMessage(`Video "${response.data.title}" uploaded successfully! ID: ${response.data.id}`);
      // Reset form (optional)
      setVideoFile(null);
      setTitle('');
      setDescription('');
      setGenre('');
      setTags('');
      setUploadProgress(0);
      // Optionally navigate to the video player page or admin video list
      // navigate(`/player/${response.data.id}`);
    } catch (err) {
      console.error("Upload failed:", err);
      setError(err.response?.data?.message || err.message || 'Video upload failed. Please try again.');
      setUploadProgress(0);
    } finally {
      setIsUploading(false);
    }
  };

  // Simple loading state for the page itself if needed (e.g. if fetching genres for a dropdown)
  // if (!currentUser) return <p>Loading user data...</p>; // or redirect handled by useEffect

  return (
    <div className="min-h-screen bg-gray-800 py-8 px-4 sm:px-6 lg:px-8 text-white">
      <div className="max-w-2xl mx-auto bg-gray-700 p-6 sm:p-8 rounded-lg shadow-xl">
        <h2 className="text-2xl sm:text-3xl font-bold text-center text-white mb-8">Upload New Video</h2>

        {error && <p className="bg-red-500 text-white p-3 rounded mb-4 text-sm text-center">{error}</p>}
        {successMessage && <p className="bg-green-500 text-white p-3 rounded mb-4 text-sm text-center">{successMessage}</p>}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="videoFile" className="block text-sm font-medium text-gray-300 mb-1">Video File*</label>
            <input
              type="file"
              id="videoFile"
              onChange={handleFileChange}
              accept="video/mp4,video/webm,video/ogg,video/quicktime,video/x-matroska" // Accept common video types
              required
              className="block w-full text-sm text-gray-400 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-netflix-red file:text-white hover:file:bg-red-700 cursor-pointer"
            />
            {videoFile && <p className="text-xs text-gray-400 mt-1">Selected: {videoFile.name} ({(videoFile.size / (1024*1024)).toFixed(2)} MB)</p>}
          </div>

          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-300 mb-1">Title*</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              className="w-full input-style"
              placeholder="Enter video title"
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-300 mb-1">Description</label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows="3"
              className="w-full input-style"
              placeholder="Enter video description"
            ></textarea>
          </div>

          <div>
            <label htmlFor="genre" className="block text-sm font-medium text-gray-300 mb-1">Genre</label>
            <input
              type="text"
              id="genre"
              value={genre}
              onChange={(e) => setGenre(e.target.value)}
              className="w-full input-style"
              placeholder="e.g., Action, Comedy, Sci-Fi"
            />
          </div>

          <div>
            <label htmlFor="tags" className="block text-sm font-medium text-gray-300 mb-1">Tags (comma-separated)</label>
            <input
              type="text"
              id="tags"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              className="w-full input-style"
              placeholder="e.g., space, future, tech"
            />
          </div>

          {isUploading && (
            <div className="w-full bg-gray-600 rounded-full h-2.5 dark:bg-gray-500 my-2">
              <div
                className="bg-netflix-red h-2.5 rounded-full transition-all duration-300 ease-out"
                style={{ width: `${uploadProgress}%` }}
              ></div>
              <p className="text-xs text-center text-gray-300 mt-1">{uploadProgress}%</p>
            </div>
          )}

          <button
            type="submit"
            disabled={isUploading || !videoFile}
            className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-netflix-red hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isUploading ? 'Uploading...' : 'Upload Video'}
          </button>
        </form>
      </div>
      {/* Helper for input styles if not globally defined */}
      <style jsx>{`
         .input-style {
             background-color: #4A5568; /* bg-gray-600 or a bit lighter */
             border: 1px solid #718096; /* border-gray-500 */
             border-radius: 0.375rem; /* rounded-md */
             padding: 0.5rem 0.75rem; /* py-2 px-3 */
             color: white;
             width: 100%;
         }
         .input-style:focus {
             outline: none;
             border-color: #E50914; /* netflix-red */
             box-shadow: 0 0 0 1px #E50914;
         }
         .input-style::placeholder {
             color: #A0AEC0; /* placeholder-gray-400 */
         }
      `}</style>
    </div>
  );
};

export default UploadVideoPage;