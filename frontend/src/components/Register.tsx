import React, { useState, useEffect } from 'react';
// NOTE: In a production app, replace the mockAuthApi with an actual HTTP client like axios or fetch.
// This uses fetch's structure but wrapped in a promise for better error simulation.

// --- TypeScript Definitions ---

// Define the shape of the data returned from a successful login/registration
interface AuthResponseData {
  status: boolean;
  message: string;
  jwt: string;
  userId: number;
  email: string;
  fullName: string;
}

// Define the shape of the user state object
interface CurrentUser {
  fullName: string;
  email: string;
  userId: number;
}

// Define the shape of the registration form data
interface FormData {
  fullName: string;
  email: string;
  password: string;
}

// --- API URL (Ensure this matches your backend port) ---

// NOTE: Using port 5454, which was used in previous backend discussion.
const API_URL = 'http://localhost:5455/auth/signup';

// --- API Client (Using actual fetch structure for clarity) ---

const callAuthApi = async (data: FormData): Promise<AuthResponseData> => {
    const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    });

    // Handle network errors or non-JSON responses first
    if (!response.ok) {
        let errorBody = await response.text();

        // Try to parse JSON error if available
        try {
            const jsonError = JSON.parse(errorBody);
            errorBody = jsonError.message || errorBody;
        } catch (e) {
            // If it's not JSON, use the plain text
        }

        // Throw an error object that mimics Axios structure for consistent handling
        throw { response: { status: response.status, data: { message: errorBody } } };
    }

    // On success, parse the data
    return await response.json() as AuthResponseData;
};

// --- Dashboard Component (The redirect target) ---

const Dashboard: React.FC<{ user: CurrentUser; handleLogout: () => void }> = ({ user, handleLogout }) => (
  <div className="min-h-screen bg-gray-100 p-8">
    <header className="flex justify-between items-center pb-6 border-b border-gray-300">
      <h1 className="text-3xl font-extrabold text-blue-600">TradeShift Dashboard</h1>
      <div className="flex items-center space-x-4">
        <span className="text-lg font-medium text-gray-700">Welcome, {user.fullName} ({user.email})</span>
        <button
          onClick={handleLogout}
          className="px-4 py-2 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-600 transition duration-300"
        >
          Logout
        </button>
      </div>
    </header>

    <main className="mt-8 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {/* Net Worth Card */}
      <div className="bg-white p-6 rounded-xl shadow-lg border border-gray-200">
        <p className="text-sm font-medium text-gray-500">NET WORTH</p>
        <h2 className="text-4xl font-bold text-gray-800 mt-1">$10,000.00</h2>
        <p className="text-xs text-green-500 mt-2">Initial Balance</p>
      </div>

      {/* Cash Balance Card */}
      <div className="bg-white p-6 rounded-xl shadow-lg border border-gray-200">
        <p className="text-sm font-medium text-gray-500">CASH BALANCE</p>
        <h2 className="text-4xl font-bold text-gray-800 mt-1">$10,000.00</h2>
        <p className="text-xs text-gray-500 mt-2">Available funds for trading</p>
      </div>

      {/* Trading Block */}
      <div className="bg-white p-6 rounded-xl shadow-lg border border-gray-200 col-span-full">
        <h3 className="text-xl font-semibold text-gray-700 mb-4">Execute Trade</h3>
        <div className="flex space-x-4">
          <input
            type="text"
            placeholder="Stock Symbol (e.g., AAPL)"
            className="flex-grow p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
          />
          <input
            type="number"
            placeholder="Quantity"
            className="w-32 p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
          />
          <button className="px-6 py-3 bg-green-500 text-white font-semibold rounded-lg shadow-md hover:bg-green-600 transition duration-300">
            BUY
          </button>
          <button className="px-6 py-3 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-600 transition duration-300">
            SELL
          </button>
        </div>
      </div>
    </main>
  </div>
);


// --- Registration Form Component (Incorporates your structure) ---

const RegistrationForm: React.FC<{ onRegisterSuccess: (data: AuthResponseData) => void }> = ({ onRegisterSuccess }) => {
  const [formData, setFormData] = useState<FormData>({ fullName: '', email: '', password: '' });
  const [error, setError] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Use the dedicated API client function
      const response = await callAuthApi(formData);

      // --- CRITICAL FIX: Save JWT and Trigger Dashboard Redirect ---
      if (response.status === true && response.jwt) {
        localStorage.setItem('jwt', response.jwt);
        onRegisterSuccess(response);
      } else {
        // This should theoretically be caught by the API client throw, but for safety
        setError(response.message || 'Registration failed with an unknown error.');
      }
      // -------------------------------------------------------------
    } catch (err: any) {
      // Improved error handling based on the thrown object from callAuthApi
      const status = err.response?.status;
      const message = err.response?.data?.message || 'Unknown network error.';

      if (status === 403) {
        setError(`Error: Access Forbidden (403). Check if '${API_URL}' is permitted in Spring SecurityConfig.`);
      } else if (status === 400 || status === 500) {
        // Displays error messages from the backend (e.g., "Email is already used...")
        setError(`Server Error: ${message}`);
      } else if (status) {
         setError(`HTTP Error: ${status}. Message: ${message}`);
      } else {
        // Catch general network errors (backend not running)
        setError(`Network Error: Failed to connect to ${API_URL}. Is the backend running?`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50">
      <div className="bg-white p-8 rounded-xl shadow-2xl w-full max-w-md">
        <h2 className="text-3xl font-bold text-center text-gray-800 mb-6">New User Registration</h2>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <input
              type="text"
              name="fullName"
              placeholder="Full Name (e.g., Jane Doe)"
              value={formData.fullName}
              onChange={handleChange}
              required
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>
          <div>
            <input
              type="email"
              name="email"
              placeholder="Email (e.g., user@example.com)"
              value={formData.email}
              onChange={handleChange}
              required
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>
          <div>
            <input
              type="password"
              name="password"
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
              required
              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-3 text-white font-bold rounded-lg shadow-md transition duration-300 ${
              loading ? 'bg-indigo-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700'
            }`}
          >
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>

        {error && (
          <div className="mt-4 p-3 bg-red-100 text-red-700 border border-red-300 rounded-lg flex items-center space-x-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            <p className="text-sm font-medium">{error}</p>
          </div>
        )}
      </div>
    </div>
  );
};


// --- Main Application Component ---

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);

  // Check for existing JWT on initial load
  useEffect(() => {
      const storedJwt = localStorage.getItem('jwt');
      if (storedJwt) {
          setIsAuthenticated(true);
          // Mock user data retrieval until we have a real /profile endpoint
          setCurrentUser({
             fullName: 'Returning Trader',
             email: 'trader@example.com',
             userId: 999
          });
      }
  }, []);

  // Handler for successful registration or login (triggers the redirect)
  const handleAuthSuccess = (data: AuthResponseData) => {
      setCurrentUser({
        fullName: data.fullName || 'New User',
        email: data.email,
        userId: data.userId,
      });
      setIsAuthenticated(true);
  };

  const handleLogout = () => {
      localStorage.removeItem('jwt');
      setIsAuthenticated(false);
      setCurrentUser(null);
  }

  // --- Render Logic ---

  if (isAuthenticated && currentUser) {
    // Renders the Dashboard component (our redirect destination)
    return <Dashboard user={currentUser} handleLogout={handleLogout} />;
  }

  if (isAuthenticated && !currentUser) {
      // Displays a loading state while user data is fetched
      return <div className="flex justify-center items-center min-h-screen text-xl font-medium">Loading user profile...</div>
  }

  // Shows the registration form
  return <RegistrationForm onRegisterSuccess={handleAuthSuccess} />;
};
