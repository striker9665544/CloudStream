// src/components/skeletons/VideoRowSkeleton.jsx
import React from 'react';
import Skeleton from 'react-loading-skeleton';
import VideoCardSkeleton from './VideoCardSkeleton';
import 'react-loading-skeleton/dist/skeleton.css';

const VideoRowSkeleton = ({ numberOfRows = 1, cardsPerRow = 5 }) => {
  return (
    <>
      {Array(numberOfRows).fill(0).map((_, rowIndex) => (
        <div className="mb-8" key={`row-skel-${rowIndex}`}>
          <Skeleton height={28} width={200} baseColor="#333" highlightColor="#555" className="mb-4" />
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {Array(cardsPerRow).fill(0).map((_, cardIndex) => (
              <VideoCardSkeleton key={`card-skel-${rowIndex}-${cardIndex}`} />
            ))}
          </div>
        </div>
      ))}
    </>
  );
};

export default VideoRowSkeleton;