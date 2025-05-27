// src/components/VideoCard.jsx
import React from 'react';
import { Link } from 'react-router-dom'; // For navigating to a player page later

const VideoCard = ({ video }) => {
  if (!video) return null;

  // Placeholder image if thumbnailUrl is missing
  const thumbnailUrl = video.thumbnailUrl || 'https://via.placeholder.com/300x170.png?text=No+Image';

  return (
    <Link to={`/player/${video.id}`} className="block group"> {/* Player page route TBD */}
      <div className="aspect-w-16 aspect-h-9 mb-2 overflow-hidden rounded-md shadow-lg transform group-hover:scale-105 transition-transform duration-300 ease-in-out">
        <img
          src={thumbnailUrl}
          alt={video.title || 'Video thumbnail'}
          className="object-cover w-full h-full"
        />
      </div>


      <h3 className="text-sm font-semibold text-white group-hover:text-red-400 line-clamp-2"
        style={{ height: '2.7em', lineHeight: '1.35em' }}


        >
        {video.title || 'Untitled Video'}
      </h3>
      {/* Optional: display genre or other info */}
      {/* <p className="text-xs text-gray-400">{video.genre}</p> */}
    </Link>
  );
};

export default VideoCard;