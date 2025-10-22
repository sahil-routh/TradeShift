// src/hooks/useAxios.ts

import Axios, {type AxiosInstance } from "axios";
import { useAuth } from "../context/AuthContext";
import { useMemo } from "react";

// FIX 1: Remove '/api' from BASE_URL. The Axios instance should only be configured
// with the host and port. The '/api' path will be added in the controller requests.
const BASE_URL = "http://localhost:5455";

/**
 * Custom hook to provide a memoized Axios instance with JWT authorization headers.
 * The instance is only recreated if the authentication token changes.
 */
const useAxios = (): AxiosInstance => {
    const { token } = useAuth();

    const instance = useMemo(() => {
        const api = Axios.create({
            baseURL: BASE_URL,
            headers: {
                "Content-Type": "application/json",
            }
        });

        // Add interceptor to dynamically apply the latest token
        api.interceptors.request.use(config => {
            if (token) {
                config.headers = config.headers || {};
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        });

        return api;
    }, [token]);

    return instance;
};

export default useAxios;