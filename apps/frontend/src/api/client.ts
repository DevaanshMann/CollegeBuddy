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
        // 401/403 etc bubble up as readable errors
        throw new Error(
            `Request failed with status ${res.status}${
                text ? `: ${text}` : ""
            }`
        );
    }

    // No content
    if (res.status === 204) {
        // @ts-expect-error
        return null;
    }

    const text = await res.text();
    if (!text) {
        // @ts-expect-error
        return null;
    }

    return JSON.parse(text) as T;
}

export const apiClient = {
    get: <T>(path: string) => request<T>("GET", path),
    post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
    put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
    delete: <T>(path: string) => request<T>("DELETE", path),
};
