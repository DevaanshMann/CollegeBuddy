import { useState } from "react";
import type { FormEvent } from "react";
import { apiClient } from "../../api/client";

type SearchResult = {
    userId: number;
    displayName: string;
    avatarUrl?: string;
    visibility?: string;
    campusDomain?: string;
};

export function SearchPage() {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    async function handleSearch(e: FormEvent) {
        e.preventDefault();
        if (!query.trim()) {
            setError("Please enter a search term");
            return;
        }

        setLoading(true);
        setStatus(null);
        setError(null);

        try {
            console.log("Searching for:", query.trim());

            const response = await apiClient.post<any>("/search", {
                query: query.trim()
            });

            console.log("Search response:", response);

            const list: SearchResult[] = Array.isArray(response)
                ? response
                : (response?.results ?? []);

            setResults(list);

            if (list.length === 0) {
                setStatus(`No classmates found for "${query.trim()}".`);
            } else {
                setStatus(`Found ${list.length} result${list.length === 1 ? '' : 's'}`);
            }
        } catch (err: any) {
            console.error("Search error:", err);
            setError(err.message ?? "Search failed");
        } finally {
            setLoading(false);
        }
    }

    async function handleConnect(userId: number, displayName: string) {
        setError(null);
        setStatus(null);

        try {
            await apiClient.post("/connections/request", {
                toUserId: userId,
                message: `Hey ${displayName}, let's connect!`,
            });
            setStatus(`Connection request sent to ${displayName}!`);
        } catch (err: any) {
            console.error("Send request error:", err);
            setError(err.message ?? "Failed to send request");
        }
    }

    return (
        <div>
            <h2>Search Classmates</h2>
            <p style={{ marginBottom: "1rem", color: "#9ca3af" }}>
                Search for students at your campus. You can search by name.
            </p>

            <form
                onSubmit={handleSearch}
                style={{ display: "flex", gap: "0.5rem", maxWidth: 480, marginBottom: "1.25rem" }}
            >
                <input
                    type="text"
                    placeholder="Search by name..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    style={{
                        flex: 1,
                        padding: "0.75rem",
                        fontSize: "1rem",
                        border: "1px solid #ddd",
                        borderRadius: "4px"
                    }}
                />
                <button
                    type="submit"
                    disabled={loading}
                    style={{
                        padding: "0.75rem 1.5rem",
                        fontSize: "1rem"
                    }}
                >
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
                            padding: "1rem",
                            borderRadius: "0.5rem",
                            border: "1px solid #374151",
                            backgroundColor: "#f9f9f9"
                        }}
                    >
                        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                            {/* Avatar */}
                            <div
                                style={{
                                    width: 48,
                                    height: 48,
                                    borderRadius: "999px",
                                    backgroundColor: "#1f2937",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    fontSize: 18,
                                    fontWeight: "bold",
                                    color: "white"
                                }}
                            >
                                {r.displayName?.charAt(0).toUpperCase() ?? "?"}
                            </div>

                            {/* Info */}
                            <div>
                                <div style={{ fontWeight: "bold", fontSize: "1.1rem" }}>
                                    {r.displayName}
                                </div>
                                {r.campusDomain && (
                                    <div style={{ fontSize: 14, color: "#666" }}>
                                        @{r.campusDomain}
                                    </div>
                                )}
                                {r.visibility && (
                                    <div style={{ fontSize: 12, color: "#9ca3af" }}>
                                        {r.visibility === "PUBLIC" ? "🌍 Public" : "🔒 Private"}
                                    </div>
                                )}
                            </div>
                        </div>

                        <button onClick={() => handleConnect(r.userId, r.displayName)}>
                            Connect
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}