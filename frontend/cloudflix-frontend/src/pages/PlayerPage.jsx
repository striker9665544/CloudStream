//src/pages/PlayerPage.jsx
import React, { useState, useEffect, useRef, useCallback } from 'react'; // Added useCallback
import { useParams, Link, useNavigate } from 'react-router-dom';
import ReactPlayer from 'react-player/lazy';
import VideoService from '../services/video.service';
import HistoryService from '../services/history.service'; // <<< IMPORT HISTORY SERVICE
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../services/api';

const PlayerPage = () => {
  const { videoId } = useParams();
  const playerRef = useRef(null); // Ref to access ReactPlayer instance
  const [videoData, setVideoData] = useState(null);
  const [videoUrl, setVideoUrl] = useState('');
  const [isLoadingPage, setIsLoadingPage] = useState(true);
  const [error, setError] = useState('');
  const { currentUser, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const hasFetchedMetadata = useRef(false);
  const [initialSeekDone, setInitialSeekDone] = useState(false); // To prevent multiple seeks onReady

  // Debounce progress updates
  const progressUpdateTimeout = useRef(null);


  // Function to report watch progress
  const reportProgress = useCallback(() => {
    if (playerRef.current && videoData && currentUser) {
      const currentTime = playerRef.current.getCurrentTime(); // Seconds
      const duration = playerRef.current.getDuration();       // Seconds

      if (currentTime > 0 && duration > 0) { // Only report if we have valid times
        const isCompleted = (duration - currentTime) < 10; // Consider completed if within 10s of end

        // Clear any existing timeout to debounce
        if (progressUpdateTimeout.current) {
            clearTimeout(progressUpdateTimeout.current);
        }

        // Set a new timeout to send update after a short delay (e.g., 500ms)
        // This prevents spamming the backend on every progress event.
        progressUpdateTimeout.current = setTimeout(async () => {
            try {
              // console.log(`Reporting progress for video ${videoId}: ${currentTime}s, completed: ${isCompleted}`);
              await HistoryService.recordOrUpdateProgress(videoId, {
                resumePositionSeconds: Math.round(currentTime),
                completed: isCompleted,
              });
            } catch (progressError) {
              console.error("Failed to report watch progress:", progressError);
              // Don't show this error to the user, it's a background task
            }
        }, 3000); // Update every 3 seconds of actual playback or on pause/end
      }
    }
  }, [videoId, videoData, currentUser]); // Dependencies for useCallback

  useEffect(() => {
    // Effect 1: Handle auth and fetch metadata & initial progress
    if (authLoading) {
      setIsLoadingPage(true);
      return;
    }
    if (!currentUser) {
      navigate('/login', { state: { from: `/player/${videoId}` } });
      return;
    }

    if (currentUser && !authLoading && !hasFetchedMetadata.current) {
      hasFetchedMetadata.current = true;
      setIsLoadingPage(true);
      setError('');
      setVideoData(null);
      setVideoUrl('');
      setInitialSeekDone(false); // Reset seek flag

      const fetchDetailsAndInitialProgress = async () => {
        try {
          // 1. Fetch Video Metadata
          const metaResponse = await VideoService.getVideoById(videoId);
          setVideoData(metaResponse.data);
          const fullStreamUrl = `${apiClient.defaults.baseURL}/videos/stream/${videoId}`;
          setVideoUrl(fullStreamUrl);

          // 2. Fetch Initial Watch Progress (Resume Time)
          try {
            const progressResponse = await HistoryService.getWatchProgressForVideo(videoId);
            if (progressResponse.data && progressResponse.data.resumePositionSeconds > 0) {
              // console.log("Resume position found:", progressResponse.data.resumePositionSeconds);
              if (playerRef.current) {
                playerRef.current.seekTo(parseFloat(progressResponse.data.resumePositionSeconds), 'seconds');
                setInitialSeekDone(true);
              } else {
                // Store resume time to apply it in onReady if playerRef not ready yet
                // This is a common pattern: store desired seek time and apply in onReady
                playerRef.current = { _initialSeekTime: progressResponse.data.resumePositionSeconds };
              }
            } else {
                setInitialSeekDone(true); // No resume time, so initial seek is "done"
            }
          } catch (progressErr) {
            if (progressErr.response && progressErr.response.status === 404) {
              // console.log("No previous watch history for this video.");
              setInitialSeekDone(true); // No history, so initial seek is "done"
            } else {
              console.error("Failed to fetch watch progress:", progressErr);
              // Don't block player for this, but log it
            }
            setInitialSeekDone(true); // Ensure this path also marks seek as done
          }
        } catch (err) {
          console.error("PlayerPage: Failed to load video metadata:", err);
          setError(err.response?.data?.message || err.message || 'Could not load video metadata.');
          hasFetchedMetadata.current = false;
        } finally {
          setIsLoadingPage(false);
        }
      };
      fetchDetailsAndInitialProgress();
    }
  }, [videoId, currentUser, authLoading, navigate]);

  // Effect 2: Reset hasFetched ref when videoId changes
  useEffect(() => {
    hasFetchedMetadata.current = false;
    setInitialSeekDone(false);
  }, [videoId]);


  const handlePlayerReady = useCallback((player) => {
    playerRef.current = player; // Assign the player instance to the ref
    // console.log("ReactPlayer: onReady triggered!");
    if (playerRef.current && playerRef.current._initialSeekTime && !initialSeekDone) {
        // console.log("Seeking to initial time in onReady:", playerRef.current._initialSeekTime);
        playerRef.current.seekTo(parseFloat(playerRef.current._initialSeekTime), 'seconds');
        delete playerRef.current._initialSeekTime; // Clean up
        setInitialSeekDone(true);
    } else {
        setInitialSeekDone(true); // Mark as done even if no seek time
    }
  }, [initialSeekDone]);

  // Cleanup progress update timeout on component unmount
  useEffect(() => {
    return () => {
        if (progressUpdateTimeout.current) {
            clearTimeout(progressUpdateTimeout.current);
        }
    };
  }, []);


  if (isLoadingPage || authLoading || !initialSeekDone) { // Also wait for initial seek attempt
    // ... loading JSX ...
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-xl">Loading player...</p>
      </div>
    );
  }
  // ... (error and !videoData rendering as before) ...
  if (error) {
    return (
      <div className="min-h-screen bg-black text-white flex flex-col justify-center items-center p-4">
        <p className="text-red-500 text-xl mb-4">Error: {error}</p>
        <Link to="/" className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded">Go to Home</Link>
      </div>
    );
  }

  if (!videoData) {
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center">
        <p className="text-xl">Video not found or details unavailable.</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black text-white flex flex-col items-center">
      <div className="w-full p-4 fixed top-0 left-0 z-50 bg-gradient-to-b from-black/70 to-transparent">
        <Link to="/" className="text-white hover:text-red-500 transition-colors text-sm">
          ← Back to Home
        </Link>
      </div>
      <div className="w-full max-w-screen-xl aspect-video mt-[60px] sm:mt-[80px]">
        {videoUrl ? (
          <ReactPlayer
            ref={playerRef} // Assign ref here
            url={videoUrl}
            playing={false} // Start paused, let user click play or autoplay (muted)
            controls={true}
            width="100%"
            height="100%"
            config={{ file: { attributes: { controlsList: 'nodownload' } } }}
            onReady={handlePlayerReady} // Handle when player is ready
            onProgress={reportProgress} // Report progress periodically
            onPause={reportProgress}    // Report progress on pause
            onEnded={reportProgress}     // Report progress when video ends (mark as completed)
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