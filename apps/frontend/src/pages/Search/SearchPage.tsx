import { useState } from "react";
import type { FormEvent } from "react";
import { apiClient } from "../../api/client";

type SearchResult = {
    userId: number;
    displayName: string;
    avatarUrl?: string;
    visibility?: string;
};

export function SearchPage() {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleSearch(e: FormEvent) {
        e.preventDefault();
        if (!query.trim()) return;

        setLoading(true);
        setStatus(null);
        setError(null);

        try {
            const raw = await apiClient.get<any>(
                `/search?query=${encodeURIComponent(query.trim())}`
            );
            const list: SearchResult[] = Array.isArray(raw)
                ? raw
                : (raw?.results ?? []);
            setResults(list);
            if (list.length === 0) {
                setStatus(`No classmates found for "${query.trim()}".`);
            }
        } catch (err: any) {
            console.error("Search error:", err);
            setError(err.message ?? "Search failed");
        } finally {
            setLoading(false);
        }
    }

    async function handleConnect(userId: number) {
        setError(null);
        setStatus(null);

        try {
            await apiClient.post("/connections/request", {
                toUserId: userId,
                message: "Hey, let's connect!",
            });
            setStatus("Connection request sent!");
        } catch (err: any) {
            console.error("Send request error:", err);
            setError(err.message ?? "Failed to send request");
        }
    }

    return (
        <div>
            <h2>Search Classmates</h2>
            <p style={{ marginBottom: "1rem", color: "#9ca3af" }}>
                Search only shows students with profiles from your campus (.edu domain).
            </p>

            <form
                onSubmit={handleSearch}
                style={{ display: "flex", gap: "0.5rem", maxWidth: 480, marginBottom: "1.25rem" }}
            >
                <input
                    type="text"
                    placeholder="Search by name or username"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                />
                <button type="submit" disabled={loading}>
                    {loading ? "Searching..." : "Search"}
                </button>
            </form>

            {status && <p style={{ color: "#9ca3af", marginBottom: "0.75rem" }}>{status}</p>}
            {error && <p style={{ color: "red", marginBottom: "0.75rem" }}>{error}</p>}

            <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                {results.map((r) => (
                    <div
                        key={r.userId}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            padding: "0.6rem 0.75rem",
                            borderRadius: "0.5rem",
                            border: "1px solid #374151",
                        }}
                    >
                        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                            <div
                                style={{
                                    width: 32,
                                    height: 32,
                                    borderRadius: "999px",
                                    backgroundColor: "#1f2937",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    fontSize: 14,
                                }}
                            >
                                {r.displayName?.charAt(0).toUpperCase() ?? "?"}
                            </div>
                            <div>
                                <div>{r.displayName}</div>
                                {r.visibility && (
                                    <div style={{ fontSize: 12, color: "#9ca3af" }}>{r.visibility}</div>
                                )}
                            </div>
                        </div>

                        <button onClick={() => handleConnect(r.userId)}>Connect</button>
                    </div>
                ))}
            </div>
        </div>
    );
}
