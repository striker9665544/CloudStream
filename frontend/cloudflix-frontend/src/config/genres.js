// src/config/genres.js

// Helper to create a URL slug (can also live here or in a utils file)
export const slugify = (text) => {
  if (!text) return '';
  return text.toString().toLowerCase()
    .replace(/\s+/g, '-')           // Replace spaces with -
    .replace(/[^\w-]+/g, '')       // Remove all non-word chars
    .replace(/--+/g, '-')           // Replace multiple - with single -
    .replace(/^-+/, '')             // Trim - from start of text
    .replace(/-+$/, '');            // Trim - from end of text
};

export const PREDEFINED_GENRE_ROWS = [
  { title: 'Hollywood Hits', tag: 'Hollywood', slug: slugify('Hollywood') },
  { title: 'Bollywood Masala', tag: 'Bollywood', slug: slugify('Bollywood') },
  { title: 'Sci-Fi Worlds', tag: 'SciFi', slug: slugify('SciFi') }, // Ensure 'SciFi' is the actual tag name in your DB
  { title: 'Adventure Awaits', tag: 'Adventure', slug: slugify('Adventure') },
  // You can add more curated rows here:
  // { title: 'Thrilling Action', tag: 'Action', slug: slugify('Action') },
  // { title: 'Laugh Out Loud Comedy', tag: 'Comedy', slug: slugify('Comedy') },
];
