import type { FormEvent } from "react";
import { useState } from "react";
import { apiClient } from "../../api/client";

type SearchResult = {
    userId: number;
    displayName: string | null;
    avatarUrl: string | null;
    visibility: "PUBLIC" | "CAMPUS_ONLY" | "PRIVATE";
};

type SearchResponse = SearchResult[];

export function SearchPage() {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResponse>([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleSearch(e: FormEvent) {
        e.preventDefault();
        setLoading(true);
        setMessage(null);
        setError(null);

        try {
            const res = await apiClient.post<SearchResponse>("/search", { query });
            setResults(res);
            if (res.length === 0) {
                setMessage("No classmates found.");
            }
        } catch (err: any) {
            setError(err.message ?? "Search failed");
        } finally {
            setLoading(false);
        }
    }

    async function sendRequest(userId: number) {
        setError(null);
        setMessage(null);
        try {
            await apiClient.post("/connections/request", {
                toUserId: userId,
                message: "Hey, let’s connect!",
            });
            setMessage("Connection request sent.");
        } catch (err: any) {
            setError(err.message ?? "Failed to send connection request");
        }
    }

    return (
        <div>
            <h2>Search Classmates</h2>

            <form
                onSubmit={handleSearch}
                style={{
                    maxWidth: 400,
                    display: "flex",
                    gap: "0.5rem",
                    marginBottom: "1rem",
                }}
            >
                <input
                    type="text"
                    placeholder="Search by name or username"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    required
                    style={{ flex: 1 }}
                />
                <button type="submit" disabled={loading}>
                    {loading ? "Searching..." : "Search"}
                </button>
            </form>

            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}

            <ul style={{ listStyle: "none", padding: 0 }}>
                {results.map((r) => (
                    <li
                        key={r.userId}
                        style={{
                            border: "1px solid #ddd",
                            borderRadius: 8,
                            padding: "0.75rem",
                            marginBottom: "0.5rem",
                            display: "flex",
                            alignItems: "center",
                            gap: "0.75rem",
                        }}
                    >
                        {r.avatarUrl && (
                            <img
                                src={r.avatarUrl}
                                alt={r.displayName ?? "avatar"}
                                style={{ width: 40, height: 40, borderRadius: "50%" }}
                            />
                        )}
                        <div style={{ flex: 1 }}>
                            <div style={{ fontWeight: "bold" }}>
                                {r.displayName ?? "(no name yet)"}
                            </div>
                            <div style={{ fontSize: "0.85rem", color: "#555" }}>
                                Visibility: {r.visibility}
                            </div>
                        </div>
                        <button onClick={() => sendRequest(r.userId)}>Connect</button>
                    </li>
                ))}
            </ul>
        </div>
    );
}
