// src/pages/PlayerPage.jsx
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import ReactPlayer from 'react-player/lazy';
import VideoService from '../services/video.service';
import HistoryService from '../services/history.service';
import RatingService from '../services/rating.service';
import CommentService from '../services/comment.service';
import { useAuth } from '../contexts/AuthContext';
import apiClient from '../services/api';

// --- Helper: Star Rating Component ---
const StarRating = ({ rating, totalStars = 5, onRate, interactive = true }) => {
  return (
    <div className="flex items-center">
      {[...Array(totalStars)].map((_, index) => {
        const starValue = index + 1;
        return (
          <svg
            key={starValue}
            onClick={() => interactive && onRate && onRate(starValue)}
            className={`w-5 h-5 sm:w-6 sm:h-6 ${starValue <= rating ? 'text-yellow-400' : 'text-gray-500'} ${interactive ? 'cursor-pointer hover:text-yellow-300' : ''}`}
            fill="currentColor"
            viewBox="0 0 20 20"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
          </svg>
        );
      })}
      {typeof rating === 'number' && rating > 0 && <span className="ml-2 text-sm text-gray-400">({rating.toFixed(1)}/{totalStars})</span>}
    </div>
  );
};

// --- Helper functions for Date Formatting ---
const robustDateParse = (dateStringOrObject) => {
  if (!dateStringOrObject) return null;
  if (dateStringOrObject instanceof Date) return dateStringOrObject;
  let parsableDateString = String(dateStringOrObject);
  if (parsableDateString.length === 19 && parsableDateString.charAt(10) === ' ') {
    parsableDateString = parsableDateString.replace(' ', 'T');
  }
  const date = new Date(parsableDateString);
  if (isNaN(date.getTime())) {
    // console.warn("RobustParse: Failed to parse date:", dateStringOrObject, "Attempted with:", parsableDateString);
    return null;
  }
  return date;
};
const formatDate = (dateInput) => {
  const date = robustDateParse(dateInput);
  if (!date) return 'Invalid Date';
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
};
const formatDateTime = (dateInput) => {
  const date = robustDateParse(dateInput);
  if (!date) return 'Invalid Date';
  return date.toLocaleString(undefined, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};


const PlayerPage = () => {
  const { videoId } = useParams();
  const playerRef = useRef(null);
  const hasFetchedData = useRef(false); // Changed name for clarity: general data fetch flag
  const progressUpdateTimeout = useRef(null);

  const [videoData, setVideoData] = useState(null);
  const [videoUrl, setVideoUrl] = useState('');
  const [isLoadingPage, setIsLoadingPage] = useState(true); // True initially
  const [error, setError] = useState('');
  const [commentsError, setCommentsError] = useState('');
  const [initialSeekDone, setInitialSeekDone] = useState(false);

  const { currentUser, loading: authLoading } = useAuth();
  const navigate = useNavigate();

  const [ratingSummary, setRatingSummary] = useState({ averageRating: 0, ratingCount: 0 });
  const [userRating, setUserRating] = useState(0);
  const [isRatingLoading, setIsRatingLoading] = useState(false);

  const [comments, setComments] = useState([]);
  const [isLoadingComments, setIsLoadingComments] = useState(false);
  const [commentPage, setCommentPage] = useState(0);
  const [totalCommentPages, setTotalCommentPages] = useState(0);
  const [newCommentText, setNewCommentText] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);

  const reportProgress = useCallback(() => {
    if (playerRef.current && videoData && currentUser && typeof playerRef.current.getCurrentTime === 'function' && typeof playerRef.current.getDuration === 'function') {
      const currentTime = playerRef.current.getCurrentTime();
      const duration = playerRef.current.getDuration();
      if (typeof currentTime === 'number' && typeof duration === 'number' && currentTime > 0 && duration > 0) {
        const isCompleted = (duration - currentTime) < 10;
        if (progressUpdateTimeout.current) clearTimeout(progressUpdateTimeout.current);
        progressUpdateTimeout.current = setTimeout(async () => {
          try {
            await HistoryService.recordOrUpdateProgress(videoId, {
              resumePositionSeconds: Math.round(currentTime), completed: isCompleted,
            });
          } catch (err) { console.error("[PlayerPage] Failed to report watch progress:", err); }
        }, 5000);
      }
    }
  }, [videoId, videoData, currentUser]);

  const fetchComments = useCallback(async (pageNumToFetch, initialLoad = false) => {
    if (!videoId) return;
    if (initialLoad) setIsLoadingComments(true);
    setCommentsError('');
    try {
      const response = await CommentService.getCommentsForVideo(videoId, pageNumToFetch, 5);
      if (response.data && response.data.content) {
        setComments(prev => pageNumToFetch === 0 ? response.data.content : [...prev, ...response.data.content]);
        setTotalCommentPages(response.data.totalPages);
        setCommentPage(pageNumToFetch); // Set current page
      } else {
        setComments(prev => pageNumToFetch === 0 ? [] : prev);
        setTotalCommentPages(0);
      }
    } catch (err) {
      console.error("[PlayerPage] Failed to fetch comments:", err);
      setCommentsError("Could not load comments.");
    } finally {
      if (initialLoad) setIsLoadingComments(false);
    }
  }, [videoId]);


  useEffect(() => {
    if (authLoading) return; // Wait for auth to resolve
    if (!currentUser) {
      navigate('/login', { state: { from: `/player/${videoId}` } });
      return;
    }

    if (hasFetchedData.current) return; // Don't refetch if already fetched for this videoId

    setIsLoadingPage(true);
    setError('');
    setVideoData(null); setVideoUrl(''); setInitialSeekDone(false);
    setRatingSummary({ averageRating: 0, ratingCount: 0 }); setUserRating(0);
    setComments([]); setCommentPage(0); setTotalCommentPages(0); setCommentsError('');

    const fetchAllPlayerData = async () => {
      try {
        hasFetchedData.current = true; // Mark that we are fetching
        const metaResponse = await VideoService.getVideoById(videoId);
        setVideoData(metaResponse.data);
        setVideoUrl(`${apiClient.defaults.baseURL}/videos/stream/${videoId}`);

        await Promise.allSettled([
          (async () => {
            try {
              const progressRes = await HistoryService.getWatchProgressForVideo(videoId);
              if (progressRes.data && progressRes.data.resumePositionSeconds > 0) {
                playerRef.current = { ...playerRef.current, _initialSeekTime: progressRes.data.resumePositionSeconds };
                if (playerRef.current && typeof playerRef.current.seekTo === 'function') {
                  playerRef.current.seekTo(parseFloat(progressRes.data.resumePositionSeconds), 'seconds');
                }
              }
            } catch (err) { if (err.response?.status !== 404) console.error("[PlayerPage] Err fetching progress:", err); }
            finally { setInitialSeekDone(true); }
          })(),
          (async () => {
            try {
              const summaryRes = await RatingService.getVideoRatingSummary(videoId);
              setRatingSummary(summaryRes.data);
            } catch (err) { console.error("[PlayerPage] Err fetching rating summary:", err); }
          })(),
          (async () => {
            try {
              const userRatingRes = await RatingService.getUserRatingForVideo(videoId);
              if (userRatingRes.data) setUserRating(userRatingRes.data.ratingValue);
            } catch (err) { if (err.response?.status !== 404) console.error("[PlayerPage] Err fetching user rating:", err); }
          })(),
          fetchComments(0, true)
        ]);
      } catch (err) {
        console.error("[PlayerPage] Failed to load video metadata:", err);
        setError(err.response?.data?.message || 'Could not load video metadata.');
        hasFetchedData.current = false; // Allow refetch on error
      } finally {
        setIsLoadingPage(false);
      }
    };

    fetchAllPlayerData();

  }, [videoId, currentUser, authLoading, navigate, fetchComments]); // fetchComments is stable due to useCallback

  // Reset fetch flag when videoId changes to allow data fetching for the new video
  useEffect(() => {
    hasFetchedData.current = false;
    setInitialSeekDone(false);
    // setIsLoadingPage(true); // This is handled by the main useEffect now
  }, [videoId]);

  const handlePlayerReady = useCallback((playerInstance) => {
    playerRef.current = playerInstance;
    if (playerRef.current && typeof playerRef.current._initialSeekTime === 'number' && !initialSeekDone) {
      playerRef.current.seekTo(parseFloat(playerRef.current._initialSeekTime), 'seconds');
      delete playerRef.current._initialSeekTime;
    }
    setInitialSeekDone(true);
  }, [initialSeekDone]);

  const handleRateVideo = async (ratingValue) => {
    if (!currentUser || isRatingLoading) return;
    setIsRatingLoading(true);
    try {
      await RatingService.addOrUpdateRating(videoId, { ratingValue });
      setUserRating(ratingValue);
      const summaryRes = await RatingService.getVideoRatingSummary(videoId);
      setRatingSummary(summaryRes.data);
    } catch (err) { console.error("[PlayerPage] Failed to submit rating:", err); }
    finally { setIsRatingLoading(false); }
  };

  const handlePostComment = async (e) => {
    e.preventDefault();
    if (!newCommentText.trim() || submittingComment) return;
    setSubmittingComment(true);
    setCommentsError('');
    try {
      await CommentService.createComment(videoId, { text: newCommentText });
      setNewCommentText('');
      await fetchComments(0, true); // Refresh comments
    } catch (err) {
      console.error("[PlayerPage] Failed to post comment:", err);
      setCommentsError("Could not post comment.");
    } finally { setSubmittingComment(false); }
  };

  useEffect(() => {
    return () => { if (progressUpdateTimeout.current) clearTimeout(progressUpdateTimeout.current); };
  }, []);

  // --- Conditional Rendering Logic ---
  if (authLoading || isLoadingPage) { // Simplified initial loading condition
    return (<div className="min-h-screen bg-black text-white flex justify-center items-center"><p className="text-xl">Loading player...</p></div>);
  }
  if (error) { // If there was a primary error fetching videoData
    return (
      <div className="min-h-screen bg-black text-white flex flex-col justify-center items-center p-4">
        <p className="text-red-500 text-xl mb-4">{error}</p>
        <Link to="/" className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded">Go to Home</Link>
      </div>
    );
  }
  if (!videoData) { // If loading is done, no error, but still no videoData
    return (
      <div className="min-h-screen bg-black text-white flex justify-center items-center"><p className="text-xl">Video not found or details unavailable.</p></div>
    );
  }

  // --- Main Return JSX ---
  return (
    <div className="min-h-screen bg-black text-white">
      <div className="flex flex-col lg:flex-row">
        <div className="w-full lg:w-2/3">
          {/* Back to Home & Player */}
          <div className="w-full p-1 sm:p-2 md:p-4 fixed top-0 left-0 z-50 bg-gradient-to-b from-black/70 to-transparent">
            <Link to="/" className="text-white hover:text-red-500 transition-colors text-sm">
              ← Back to Home
            </Link>
          </div>
          <div className="w-full aspect-video mt-[50px] sm:mt-[60px]">
            {videoUrl && initialSeekDone ? ( // Ensure initialSeekDone is true before showing player
              <ReactPlayer
                ref={playerRef} url={videoUrl} playing={false} controls={true} width="100%" height="100%"
                config={{ file: { attributes: { controlsList: 'nodownload' } } }}
                onReady={handlePlayerReady} onProgress={reportProgress} onPause={reportProgress} onEnded={reportProgress}
                onError={(e) => { console.error('ReactPlayer Error:', e); setError('Video playback error.'); }}
              />
            ) : (
              <div className="w-full h-full bg-gray-800 flex justify-center items-center"><p>Preparing video stream...</p></div>
            )}
          </div>

          {/* Video Info & Rating Section */}
          <div className="p-4">
            <h1 className="text-xl sm:text-2xl md:text-3xl font-bold mb-1 text-white">{videoData.title}</h1>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs sm:text-sm text-gray-400 mb-3">
              <span>{videoData.viewCount || 0} views</span>
              <span>•</span>
              <span>{formatDate(videoData.uploadTimestamp)}</span>
              {videoData.genre && ( <><span>•</span><span>{videoData.genre}</span></> )}
            </div>
            <p className="text-sm text-gray-300 mb-4 whitespace-pre-line">{videoData.description}</p>
            
            <div className="my-4 p-4 bg-gray-700 rounded-md">
              <h3 className="text-lg font-semibold mb-2 text-white">Rate this video</h3>
              <div className="flex items-center gap-4 mb-2">
                <StarRating rating={userRating} onRate={handleRateVideo} interactive={!isRatingLoading && !!currentUser}/>
                {isRatingLoading && <p className="text-xs text-gray-400">Saving...</p>}
              </div>
              <p className="text-xs text-gray-500">
                Average: {ratingSummary.averageRating.toFixed(1)}/5 ({ratingSummary.ratingCount} ratings)
              </p>
            </div>
          </div>
        </div>

        {/* Comments Section */}
        <div className="w-full lg:w-1/3 p-4 lg:max-h-screen lg:overflow-y-auto">
          <h2 className="text-xl font-semibold mb-4 text-white mt-4 lg:mt-[60px]">
             Comments ({(comments && comments.length > 0) ? comments.reduce((acc, c) => acc + 1 + (c.replyCount || 0), 0) : 0})
          </h2>
          {currentUser && (
            <form onSubmit={handlePostComment} className="mb-6">
              <textarea value={newCommentText} onChange={(e) => setNewCommentText(e.target.value)} placeholder="Add a comment..." rows="3" className="w-full p-2 bg-gray-700 text-white border border-gray-600 rounded-md focus:ring-netflix-red focus:border-netflix-red" required />
              <button type="submit" disabled={submittingComment || !newCommentText.trim()} className="mt-2 px-4 py-2 bg-netflix-red text-white rounded-md hover:bg-red-700 disabled:opacity-50">
                {submittingComment ? 'Posting...' : 'Comment'}
              </button>
            </form>
          )}
          {isLoadingComments && comments.length === 0 && <p className="text-gray-400">Loading comments...</p>}
          {commentsError && <p className="text-red-400 py-2">{commentsError}</p>}
          {comments.length > 0 ? (
            comments.map(comment => (
              <div key={comment.id} className="mb-4 p-3 bg-gray-800 rounded-md">
                <div className="flex items-center mb-1">
                  <div className="w-8 h-8 rounded-full bg-gray-600 mr-3 flex items-center justify-center text-sm font-semibold">
                    {comment.author?.displayName?.charAt(0).toUpperCase() || 'U'}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-white">{comment.author?.displayName || 'User'}</p>
                    <p className="text-xs text-gray-500">{formatDateTime(comment.createdAt)}</p>
                  </div>
                </div>
                <p className="text-sm text-gray-300 whitespace-pre-line">{comment.text}</p>
              </div>
            ))
          ) : ( !isLoadingComments && !commentsError && <p className="text-gray-400">No comments yet. Be the first!</p> )}
          {commentPage < totalCommentPages - 1 && !isLoadingComments && !isLoadingPage && (
            <div className="text-center mt-4">
                <button onClick={() => fetchComments(commentPage + 1)} disabled={isLoadingComments} className="w-full px-4 py-2 text-sm border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700">
                {isLoadingComments ? 'Loading...' : 'Load More Comments'} 
                </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PlayerPage;