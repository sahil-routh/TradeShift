import React, { createContext, useState, useEffect, useContext, ReactNode } from 'react';

// Define the shape of the user data
interface User {
  email: string;
  id?: number; // Added optional id
  name?: string; // Added optional name
}

// Define the shape of the context object
interface AuthContextType {
  user: User | null;
  // CRITICAL ADDITION: Expose the token
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, userData: User) => void;
  logout: () => void;
}

// Default context values
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Utility for token storage keys
const TOKEN_KEY = 'jwtTradeShiftToken';
const USER_KEY = 'jwtTradeShiftUser';

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [authToken, setAuthToken] = useState<string | null>(null); // New state for token
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // --- CRITICAL EFFECT HOOK ---
  useEffect(() => {
    const initializeAuth = () => {
      const token = localStorage.getItem(TOKEN_KEY);
      const userJson = localStorage.getItem(USER_KEY);

      if (token && userJson) {
        try {
            const savedUser: User = JSON.parse(userJson);

            // Basic validation check
            if (savedUser.email) {
                // Set the token state
                setAuthToken(token);
                setUser(savedUser);
                setIsAuthenticated(true);
            } else {
                // If user data is invalid, clear storage
                localStorage.removeItem(TOKEN_KEY);
                localStorage.removeItem(USER_KEY);
            }
        } catch (e) {
            console.error("Failed to parse user data from storage:", e);
            // If parsing fails, clear storage
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
        }
      } else {
          // If token or userJson is missing, ensure both are cleared
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(USER_KEY);
      }

      // *** THE UNBLOCK: This line MUST run at the end of the check. ***
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  // Login function
  const login = (token: string, userData: User) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(userData));

    setAuthToken(token); // Set token in state
    setUser(userData);
    setIsAuthenticated(true);
    // Do NOT set isLoading to false here, it should already be false from the useEffect,
    // or we assume it was implicitly set during the login transition.
    // Keeping it clean: setIsLoading(false); // Removed to rely on useEffect initialization state
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setAuthToken(null); // Clear token state
    setUser(null);
    setIsAuthenticated(false);
  };

  const value = {
    user,
    token: authToken, // Expose the token
    isAuthenticated,
    isLoading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use the Auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
