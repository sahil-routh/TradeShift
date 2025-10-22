import React, { useEffect, ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react'; // Assuming lucide-react is installed now

interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Only check authentication status AFTER the AuthContext has initialized
    if (!isLoading && !isAuthenticated) {
      // If not authenticated after checking local storage, redirect to login
      navigate('/login', { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate]);

  // If the context is not ready, show a loading spinner instead of the content
  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loader2 className="animate-spin text-indigo-600 h-10 w-10" />
        <p className="ml-3 text-indigo-600">Loading user session...</p>
      </div>
    );
  }

  // If ready and authenticated, show the children (Dashboard)
  if (isAuthenticated) {
    return <>{children}</>;
  }

  // If ready but not authenticated, the useEffect hook will handle the redirect.
  // We can return null here as a fallback until the redirect happens.
  return null;
};

export default ProtectedRoute;
