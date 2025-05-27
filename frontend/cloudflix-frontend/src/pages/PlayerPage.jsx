// src/pages/PlayerPage.jsx
import React, { useState, useEffect, useRef } from 'react'; // Added useRef
import { useParams, Link, useNavigate } from 'react-router-dom';
import ReactPlayer from 'react-player/lazy';
import VideoService from '../services/video.service';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../services/api';

const PlayerPage = () => {
  const { videoId } = useParams();
  const [videoData, setVideoData] = useState(null);
  const [videoUrl, setVideoUrl] = useState('');
  const [isLoadingPage, setIsLoadingPage] = useState(true); // Overall page loading
  const [error, setError] = useState('');
  const { currentUser, loading: authLoading } = useAuth(); // Get authLoading from context
  const navigate = useNavigate();

  // To prevent multiple calls if videoId or currentUser doesn't change
  const hasFetched = useRef(false);

  useEffect(() => {
    // Effect 1: Handle auth state and initial redirection/fetch trigger
    // console.log("[Effect 1] Running. videoId:", videoId, "authLoading:", authLoading, "currentUser:", !!currentUser, "hasFetched.current:", hasFetched.current);

    if (authLoading) {
      // console.log("[Effect 1] Auth is loading, waiting...");
      setIsLoadingPage(true); // Keep showing loading screen
      return; // Wait for auth context
    }

    if (!currentUser) {
      // console.log("[Effect 1] No currentUser and auth is done. Redirecting to login.");
      navigate('/login', { state: { from: `/player/${videoId}` } });
      return; // Redirect
    }

    // If we have a user and auth is done, and we haven't fetched for this videoId yet
    if (currentUser && !authLoading && !hasFetched.current) {
      // console.log("[Effect 1] Conditions met to fetch video details for videoId:", videoId);
      hasFetched.current = true; // Mark as attempting to fetch
      setIsLoadingPage(true); // Set loading before fetch
      setError('');
      setVideoData(null);
      setVideoUrl('');

      VideoService.getVideoById(videoId)
        .then(metaResponse => {
          // console.log("[Effect 1] Metadata response received:", metaResponse.data);
          setVideoData(metaResponse.data);
          const fullStreamUrl = `${apiClient.defaults.baseURL}/videos/stream/${videoId}`;
          // console.log("[Effect 1] Constructed stream URL:", fullStreamUrl);
          setVideoUrl(fullStreamUrl);
        })
        .catch(err => {
          console.error("PlayerPage: Failed to load video metadata:", err);
          setError(err.response?.data?.message || err.message || 'Could not load video metadata.');
          hasFetched.current = false; // Allow retry if error, or handle differently
        })
        .finally(() => {
          // console.log("[Effect 1] Fetch attempt finished. Setting isLoadingPage to false.");
          setIsLoadingPage(false);
        });
    }
  }, [videoId, currentUser, authLoading, navigate]); // Dependencies that trigger re-evaluation

  // Effect 2: Reset hasFetched ref when videoId changes, so new video data is fetched
  useEffect(() => {
    // console.log("[Effect 2] videoId changed to:", videoId, ". Resetting hasFetched.");
    hasFetched.current = false;
    // We might also want to set isLoadingPage to true here to show loading for new videoId
    // setIsLoadingPage(true); // Consider if needed, might cause flicker if Effect 1 handles it
  }, [videoId]);


  // Debug log for videoUrl state
  useEffect(() => {
    if (videoUrl) {
      // console.log("ReactPlayer URL being used (from videoUrl state):", videoUrl);
    }
  }, [videoUrl]);


  if (isLoadingPage || authLoading) { // Check both general page loading and auth loading
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-xl">Loading player...</p>
      </div>
    );
  }

  if (error) {
    // ... your error display JSX ...
    return (
      <div className="min-h-screen bg-black text-white flex flex-col justify-center items-center p-4">
        <p className="text-red-500 text-xl mb-4">Error: {error}</p>
        <Link to="/" className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded">Go to Home</Link>
      </div>
    );
  }

  if (!videoData) { // If not loading, no error, but no videoData (e.g., API returned empty for valid ID, or initial state)
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-xl">Video not found or details unavailable.</p>
      </div>
    );
  }

  return (
    // ... your main return JSX for player and videoData ...
    <div className="min-h-screen bg-black text-white flex flex-col items-center">
      <div className="w-full p-4 fixed top-0 left-0 z-50 bg-gradient-to-b from-black/70 to-transparent">
        <Link to="/" className="text-white hover:text-red-500 transition-colors text-sm">
          ← Back to Home
        </Link>
      </div>
      <div className="w-full max-w-screen-xl aspect-video mt-[60px] sm:mt-[80px]">
        {videoUrl ? (
          <ReactPlayer
            url={videoUrl}
            playing={false}
            controls={true}
            width="100%"
            height="100%"
            config={{ file: { attributes: { controlsList: 'nodownload' } } }}
            onReady={() => console.log("ReactPlayer: onReady!")}
            onStart={() => console.log("ReactPlayer: onStart!")}
            onError={(e) => { console.error('ReactPlayer Error:', e); setError('Video playback error.'); }}
          />
        ) : (
          <div className="w-full h-full bg-gray-800 flex justify-center items-center">
            <p>Preparing video stream...</p>
          </div>
        )}
      </div>
      {videoData && (
        <div className="w-full max-w-screen-xl p-4 mt-4">
            <h1 className="text-2xl sm:text-3xl font-bold mb-2">{videoData.title}</h1>
            <p className="text-gray-400 text-sm mb-4">{videoData.description}</p>
        </div>
      )}
    </div>
  );
};

export default PlayerPage;