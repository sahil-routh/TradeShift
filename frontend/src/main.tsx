import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App'; // Import the main App component
import './index.css'; // Import your Tailwind CSS or global styles

// Ensure you have an element with the id 'root' in your public/index.html (or similar HTML file)

const container = document.getElementById('root');
if (container) {
  const root = createRoot(container);
  // Render the App component
  root.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
} else {
    // This message helps debugging if the root element is missing
    console.error('Failed to find the root element to mount the React application.');
}
