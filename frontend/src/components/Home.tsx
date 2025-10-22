import React from 'react';
import { useNavigate } from 'react-router-dom';

// CORRECTED PATH: '../' goes up one directory from 'src/components' to 'src'
import TradingImage from '../a-business-man-standing-in-the-stock-market-realistic-image-ultra-hd-high-design-very-detailed-free-photo.jpg';


export default function Home() {
    const navigate=useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-r from-indigo-50 to-white flex flex-col">
      <header className="container mx-auto p-6 flex justify-between items-center text-indigo-700">
        <h1 className="text-3xl font-bold">TradeShift</h1>
        <nav>

          <button onClick={() => navigate('/login')} className="text-indigo-600 hover:text-indigo-800 mr-4 font-medium">Login</button>
          <button onClick={() => navigate('/register')} className="bg-indigo-600 text-white py-2 px-5 rounded-lg hover:bg-indigo-700 transition">Register</button>
        </nav>
      </header>

      <main className="flex-grow container mx-auto flex flex-col lg:flex-row items-center px-6 py-16 gap-10">
        <div className="lg:w-1/2 space-y-6">
          <h2 className="text-5xl font-extrabold text-indigo-800 leading-tight">
            All-in-one Financial Portfolio and Trading Platform
          </h2>
          <p className="text-lg text-gray-700 max-w-xl">
            Securely connect brokerage accounts, track real-time portfolio performance, analyze stock data, and execute trades seamlessly.
          </p>
          <div>
            <button className="bg-indigo-600 text-white py-3 px-8 rounded-lg shadow-lg hover:bg-indigo-700 transition mr-4">
              Get Started
            </button>
            <button className="py-3 px-8 rounded-lg border border-indigo-600 text-indigo-600 hover:bg-indigo-100 transition">
              Learn More
            </button>
          </div>
        </div>
        <div className="lg:w-1/2">
          <img
            // Use the imported variable
            src={TradingImage}
            alt="Trader managing financial portfolio"
            className="rounded-lg shadow-2xl w-full h-auto"
          />
        </div>
      </main>

      <footer className="bg-indigo-100 text-indigo-600 p-6 text-center">
        © 2025 TradeShift - Financial Portfolio Management
      </footer>
    </div>
  );
}