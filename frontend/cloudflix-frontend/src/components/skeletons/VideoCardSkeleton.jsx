// src/components/skeletons/VideoCardSkeleton.jsx
import React from 'react';
import Skeleton from 'react-loading-skeleton';
import 'react-loading-skeleton/dist/skeleton.css';

const VideoCardSkeleton = () => {
  return (
    <div className="block">
      <div className="aspect-w-16 aspect-h-9 mb-2">
        <Skeleton height="100%" baseColor="#202020" highlightColor="#444" />
      </div>
      <Skeleton height={20} width="80%" baseColor="#333" highlightColor="#555" />
    </div>
  );
};

export default VideoCardSkeleton;