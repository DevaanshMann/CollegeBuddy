// src/api/client.ts
import { API_BASE_URL, JWT_STORAGE_KEY } from "../config";

async function request<T>(
    method: "GET" | "POST" | "PUT" | "DELETE",
    path: string,
    body?: unknown
): Promise<T> {
    const token = localStorage.getItem(JWT_STORAGE_KEY);

    const headers: Record<string, string> = {
        "Content-Type": "application/json",
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE_URL}${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(
            `Request failed with status ${res.status}${text ? `: ${text}` : ""}`
        );
    }

    // Handle responses with no content
    if (res.status === 204 || res.headers.get("content-length") === "0") {
        return undefined as T;
    }

    // Check if response has content before parsing JSON
    const contentType = res.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return (await res.json()) as T;
    }

    // If no content-type or not JSON, return undefined
    return undefined as T;
}

export const apiClient = {
    get: <T>(path: string) => request<T>("GET", path),
    post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
    put:  <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
    del:  <T>(path: string) => request<T>("DELETE", path),
};
