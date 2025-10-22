import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../context/AuthContext';

export default function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage("");

    try {
      // FIX 1: Correct the URL path: remove '/api' since AuthController is mapped to '/auth'
      const response = await fetch("http://localhost:5455/auth/signin", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();

        const receivedToken = data.jwt;

        // FIX 2: Extract ALL user data returned by the backend (userId, email, fullName)
        const userData = {
            email: data.email,
            id: data.userId,
            name: data.fullName
        };

        if (receivedToken) {
            // FIX 3: Pass the token and full userData to the context login function
            login(receivedToken, userData);

            // 4. Redirect to the Dashboard after successful login
            navigate("/dashboard");
        }
        // Optional: Handle 2FA response if needed
        else if (data.twoFactorAuthEnabled) {
             setMessage("Two-Factor Authentication enabled. Please check your email for OTP.");
             // navigate("/verify-otp?session=" + data.session);
        }
        else {
            setMessage("Login successful, but token missing or unexpected response structure.");
        }

      } else {
        // ... Error handling remains robust ...
        const errorText = await response.text();
        try {
             // Try to parse JSON error (e.g., Spring Security BadCredentialsException)
            const errorData = JSON.parse(errorText);
            setMessage(errorData.message || errorData.error || "Invalid email or password.");
        } catch {
            // Fallback for plain text or network issues
            setMessage(`Login failed: Invalid email or password.`);
        }
      }
    } catch (error) {
      console.error("Login error:", error);
      setMessage("Network connection failed. Check if the backend server is running on port 5455.");
    }
  };

  return (
    <div className="max-w-sm mx-auto p-6 bg-white rounded-xl shadow-2xl mt-10 border border-indigo-200">
      <h2 className="text-2xl font-bold text-indigo-700 mb-6 text-center">User Login</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-3 border border-gray-300 rounded-lg focus:ring-indigo-500 focus:border-indigo-500"
          required
        />
        <button
          type="submit"
          className="w-full bg-indigo-600 text-white py-3 rounded-lg hover:bg-indigo-700 transition shadow-md font-semibold"
        >
          Login
        </button>
      </form>
      {message && (
        <p className="mt-4 p-3 bg-red-100 text-red-700 rounded-lg text-sm font-medium text-center">{message}</p>
      )}
    </div>
  );
}