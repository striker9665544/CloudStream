// src/components/VideoRow.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import VideoCard from './VideoCard';

// Import Swiper React components
import { Swiper, SwiperSlide } from 'swiper/react';

// Import Swiper styles
import 'swiper/css';
import 'swiper/css/navigation'; // For navigation arrows
import 'swiper/css/pagination';  // For pagination dots

// Import required modules
import { Navigation, Pagination } from 'swiper/modules'; // <<<< THIS LINE UNCOMMENTED

const VideoRow = ({ title, videos, genreSlug }) => {
  if (!videos || videos.length === 0) {
    return null;
  }

  return (
    <div className="mb-8 video-row">
      <div className="flex justify-between items-center mb-3 sm:mb-4">
        <h2 className="text-lg sm:text-xl font-semibold text-white">{title}</h2>
        {genreSlug && (
          <Link
            to={`/genre/${genreSlug}`}
            className="text-xs sm:text-sm text-red-400 hover:text-red-300 transition-colors"
          >
            See All →
          </Link>
        )}
      </div>

      <Swiper
        spaceBetween={16}
        slidesPerView={'auto'}
        navigation // Enables navigation arrows
        pagination={{ clickable: true }} // Enables clickable pagination dots
        modules={[Navigation, Pagination]} // Registers the modules with Swiper
        className="mySwiper"
        breakpoints={{
          320: { slidesPerView: 2.3, spaceBetween: 10 },
          640: { slidesPerView: 3.3, spaceBetween: 12 },
          768: { slidesPerView: 4.3, spaceBetween: 16 },
          1024: { slidesPerView: 5.3, spaceBetween: 16 },
          1280: { slidesPerView: 6.3, spaceBetween: 16 },
        }}
      >
        {videos.map((video) => (
          <SwiperSlide key={video.id} style={{ width: 'auto' }}>
            <div className="w-[150px] sm:w-[180px] md:w-[200px]">
              <VideoCard video={video} />
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
};

export default VideoRow;