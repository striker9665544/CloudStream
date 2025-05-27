// src/components/skeletons/BannerSkeleton.jsx
import React from 'react';
import Skeleton from 'react-loading-skeleton';
import 'react-loading-skeleton/dist/skeleton.css'; // Import skeleton CSS

const BannerSkeleton = () => {
  return (
    <div className="relative h-[50vh] sm:h-[60vh] md:h-[70vh] lg:h-[80vh] mb-8">
      <Skeleton height="100%" baseColor="#202020" highlightColor="#444" />
      <div className="absolute bottom-10 left-6 sm:left-10 p-4 max-w-xl z-10">
        <Skeleton height={40} width={300} baseColor="#333" highlightColor="#555" className="mb-3" />
        <Skeleton count={2} height={20} baseColor="#333" highlightColor="#555" className="mb-4" />
        <Skeleton height={40} width={100} baseColor="#333" highlightColor="#555" />
      </div>
    </div>
  );
};

export default BannerSkeleton;