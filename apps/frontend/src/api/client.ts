import { API_BASE_URL, JWT_STORAGE_KEY } from "../config";

async function request<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {
    const token = localStorage.getItem(JWT_STORAGE_KEY);

    const baseHeaders = options.headers ?? {};

    // Use a plain record so we can index with ["Authorization"]
    const headers: Record<string, string> = {
        "Content-Type": "application/json",
        ...(baseHeaders as Record<string, string>),
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
    });

    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Request failed with status ${res.status}`);
    }

    if (res.status === 204) {
        return {} as T;
    }

    return (await res.json()) as T;
}

export const apiClient = {
    get<T>(path: string) {
        return request<T>(path);
    },
    post<T>(path: string, body?: unknown) {
        return request<T>(path, {
            method: "POST",
            body: body ? JSON.stringify(body) : undefined,
        });
    },
    put<T>(path: string, body?: unknown) {
        return request<T>(path, {
            method: "PUT",
            body: body ? JSON.stringify(body) : undefined,
        });
    },
};
