// src/components/Banner.jsx (or keep inline in HomePage.jsx and adjust imports)
import React from 'react';
import { Link } from 'react-router-dom';

const Banner = ({ video }) => {
  if (!video) return null; // Or a placeholder/skeleton if preferred when video is null but not loading

  const thumbnailUrl = video.thumbnailUrl || 'https://via.placeholder.com/1280x720.png?text=Featured+Video';
  const description = video.description || "No description available.";

  return (
    <div className="relative h-[50vh] sm:h-[60vh] md:h-[70vh] lg:h-[80vh] text-white">
      {/* Background Image */}
      <img
        src={thumbnailUrl}
        alt={video.title || 'Featured content'}
        className="absolute inset-0 w-full h-full object-cover -z-10" // -z-10 to be behind text & overlay
      />
      {/* Dark Overlay for contrast */}
      <div className="absolute inset-0 bg-black opacity-40"></div>
      {/* Gradient Overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-gray-900 via-gray-900/70 to-transparent"></div>

      {/* Content */}
      <div className="absolute bottom-[10%] sm:bottom-[15%] left-4 md:left-10 lg:left-16 p-4 max-w-xs sm:max-w-md md:max-w-lg lg:max-w-xl z-10">
        <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold mb-2 md:mb-4 line-clamp-2">
          {video.title || 'Featured Title'}
        </h1>
        <p className="text-xs sm:text-sm md:text-base text-gray-200 mb-4 md:mb-6 line-clamp-2 sm:line-clamp-3">
          {description}
        </p>
        <div className="flex space-x-3">
          <Link
            to={`/player/${video.id}`} // Player page route TBD
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 sm:px-6 sm:py-2.5 rounded-md text-sm sm:text-base font-semibold transition-colors flex items-center"
          >
            {/* Optional: Play Icon */}
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5 sm:w-6 sm:h-6 mr-2">
              <path fillRule="evenodd" d="M4.5 5.653c0-1.427 1.529-2.33 2.779-1.643l11.54 6.347c1.295.712 1.295 2.573 0 3.286L7.28 19.99c-1.25.687-2.779-.217-2.779-1.643V5.653Z" clipRule="evenodd" />
            </svg>
            Play
          </Link>
          {/* <Link
            to={`/details/${video.id}`} // Details page route TBD
            className="bg-gray-500 bg-opacity-70 hover:bg-gray-600 text-white px-4 py-2 sm:px-6 sm:py-2.5 rounded-md text-sm sm:text-base font-semibold transition-colors flex items-center"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5 sm:w-6 sm:h-6 mr-2">
              <path fillRule="evenodd" d="M2.25 12c0-5.385 4.365-9.75 9.75-9.75s9.75 4.365 9.75 9.75-4.365 9.75-9.75 9.75S2.25 17.385 2.25 12Zm8.706-1.442c1.146-.573 2.437.463 2.126 1.706l-.709 2.836.042-.02a.75.75 0 0 1 .67 1.34l-.042.022c-1.147.573-2.438-.463-2.127-1.706l.71-2.836-.042.02a.75.75 0 1 1-.67-1.34l.042-.022ZM12 9a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Z" clipRule="evenodd" />
            </svg>
            More Info
          </Link> */}
        </div>
      </div>
    </div>
  );
};

export default Banner;