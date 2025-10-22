import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Home from './components/Home';
import LoginForm from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// 🌟 NEW IMPORT: Import the component for the new page
import TransactionHistory from './components/TransactionHistory';

// --- Navigation component to show conditional links ---
const Navigation = () => {
  const { isLoggedIn, logout, userEmail } = useAuth();

  return (
    <nav className="bg-white shadow p-4 flex justify-between items-center container mx-auto">
      <div className="flex gap-4">
        <Link to="/" className="text-indigo-600 font-semibold hover:underline">Home</Link>
        {isLoggedIn && (
          <Link to="/dashboard" className="text-indigo-600 font-semibold hover:underline">Dashboard</Link>
        )}
      </div>

      <div className="flex items-center gap-4">
        {isLoggedIn ? (
          <>
            <span className="text-sm text-gray-600 font-medium hidden sm:block">Welcome, {userEmail}</span>
            <button
              onClick={logout}
              className="bg-red-500 text-white py-2 px-4 rounded-lg hover:bg-red-600 transition"
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="text-indigo-600 hover:text-indigo-800 mr-2 font-medium">Login</Link>
            <Link to="/register" className="bg-indigo-600 text-white py-2 px-4 rounded-lg hover:bg-indigo-700 transition">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};

// --- Main Application Component ---
const AppContent = () => {
  return (
    <>
      <Navigation />
      <main className="container mx-auto p-6">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Route for Dashboard */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          } />

          {/* 🚀 NEW ROUTE: Transaction History Page */}
          <Route
            path="/history"
            element={
              <ProtectedRoute>
                <TransactionHistory />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<h1 className="text-center text-xl pt-10">404 - Page Not Found</h1>} />
        </Routes>
      </main>
    </>
  );
};

// --- Export default with BrowserRouter and AuthProvider ---
export default function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <BrowserRouter>
        <AuthProvider>
          <AppContent />
        </AuthProvider>
      </BrowserRouter>
    </div>
  );
}