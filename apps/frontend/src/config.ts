// apps/frontend/src/config.ts
export const API_BASE_URL =
    (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, "") ||
    "http://localhost:8080";

export const JWT_STORAGE_KEY = "collegebuddy_jwt";
