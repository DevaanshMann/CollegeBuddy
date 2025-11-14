export const API_BASE_URL =
    (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, "") ||
    "http://localhost:8081";  // Changed from 8080 to 8081

export const JWT_STORAGE_KEY = "collegebuddy_jwt";