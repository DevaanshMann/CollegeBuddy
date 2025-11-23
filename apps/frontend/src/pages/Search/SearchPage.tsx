import { useState, useEffect } from "react";
import type { FormEvent } from "react";
import { apiClient } from "../../api/client";
import { JWT_STORAGE_KEY } from "../../config";

type SearchResult = {
    userId: number;
    displayName: string;
    avatarUrl?: string;
    visibility?: string;
    campusDomain?: string;
};

type UserDto = {
    userId: number;
    displayName: string;
    avatarUrl?: string;
    visibility?: string;
    campusDomain?: string;
};

type ConnectionsResponse = {
    connections: UserDto[];
    incomingRequests: UserDto[];
    outgoingRequests: UserDto[];
};

export function SearchPage() {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [connectionsData, setConnectionsData] = useState<ConnectionsResponse>({
        connections: [],
        incomingRequests: [],
        outgoingRequests: []
    });
    const [confirmDisconnect, setConfirmDisconnect] = useState<{ userId: number; displayName: string } | null>(null);

    // Decode JWT to get current user ID
    const getUserIdFromToken = () => {
        const token = localStorage.getItem(JWT_STORAGE_KEY);
        if (!token) return null;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.sub;
        } catch (e) {
            console.error("Failed to decode token:", e);
            return null;
        }
    };

    // Load connections data on component mount
    useEffect(() => {
        const userId = getUserIdFromToken();
        if (userId) {
            setCurrentUserId(Number(userId));
        }

        async function loadConnections() {
            try {
                const res = await apiClient.get<ConnectionsResponse>("/connections");
                setConnectionsData(res);
            } catch (err: any) {
                console.error("Failed to load connections:", err);
            }
        }

        void loadConnections();
    }, []);

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

            // Reload connections to update status
            const res = await apiClient.get<ConnectionsResponse>("/connections");
            setConnectionsData(res);
        } catch (err: any) {
            console.error("Send request error:", err);
            setError(err.message ?? "Failed to send request");
        }
    }

    function showDisconnectConfirm(userId: number, displayName: string) {
        setConfirmDisconnect({ userId, displayName });
    }

    async function handleDisconnect() {
        if (!confirmDisconnect) return;

        setError(null);
        setStatus(null);

        try {
            await apiClient.del(`/connections/${confirmDisconnect.userId}`);
            setStatus(`Disconnected from ${confirmDisconnect.displayName}`);
            setConfirmDisconnect(null);

            // Reload connections to update status
            const res = await apiClient.get<ConnectionsResponse>("/connections");
            setConnectionsData(res);
        } catch (err: any) {
            console.error("Disconnect error:", err);
            setError(err.message ?? "Failed to disconnect");
            setConfirmDisconnect(null);
        }
    }

    // Helper function to determine connection status
    const getConnectionStatus = (userId: number): "you" | "connected" | "pending" | "connect" => {
        if (userId === currentUserId) {
            return "you";
        }
        if (connectionsData.connections.some(c => c.userId === userId)) {
            return "connected";
        }
        if (connectionsData.outgoingRequests.some(r => r.userId === userId)) {
            return "pending";
        }
        return "connect";
    };

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
                {results.map((r) => {
                    const connectionStatus = getConnectionStatus(r.userId);

                    return (
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

                            {/* Connection Status / Action */}
                            {connectionStatus === "you" && (
                                <span style={{
                                    padding: "0.5rem 1rem",
                                    fontSize: "0.9rem",
                                    color: "#666",
                                    fontWeight: "bold"
                                }}>
                                    You
                                </span>
                            )}
                            {connectionStatus === "connected" && (
                                <button
                                    onClick={() => showDisconnectConfirm(r.userId, r.displayName)}
                                    style={{
                                        backgroundColor: "#dc2626",
                                        color: "white",
                                        border: "none",
                                        padding: "0.5rem 1rem",
                                        borderRadius: "4px",
                                        cursor: "pointer",
                                        fontSize: "0.9rem",
                                        fontWeight: "bold"
                                    }}
                                >
                                    Disconnect
                                </button>
                            )}
                            {connectionStatus === "pending" && (
                                <span style={{
                                    padding: "0.5rem 1rem",
                                    fontSize: "0.9rem",
                                    color: "#f59e0b",
                                    fontWeight: "bold"
                                }}>
                                    Pending
                                </span>
                            )}
                            {connectionStatus === "connect" && (
                                <button onClick={() => handleConnect(r.userId, r.displayName)}>
                                    Connect
                                </button>
                            )}
                        </div>
                    );
                })}
            </div>

            {/* Disconnect Confirmation Modal */}
            {confirmDisconnect && (
                <div
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: "rgba(0, 0, 0, 0.5)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        zIndex: 1000,
                    }}
                >
                    <div
                        style={{
                            backgroundColor: "white",
                            padding: "2rem",
                            borderRadius: "0.5rem",
                            maxWidth: "400px",
                            textAlign: "center",
                            boxShadow: "0 4px 20px rgba(0, 0, 0, 0.15)",
                        }}
                    >
                        <h3 style={{ marginBottom: "1rem" }}>Confirm Disconnect</h3>
                        <p style={{ marginBottom: "1.5rem", color: "#666" }}>
                            Are you sure you want to disconnect from <strong>{confirmDisconnect.displayName}</strong>?
                            You will need to send a new connection request to reconnect.
                        </p>
                        <div style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
                            <button
                                onClick={() => setConfirmDisconnect(null)}
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    borderRadius: "4px",
                                    border: "1px solid #ddd",
                                    backgroundColor: "white",
                                    color: "#333",
                                    cursor: "pointer",
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDisconnect}
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    borderRadius: "4px",
                                    border: "none",
                                    backgroundColor: "#dc2626",
                                    color: "white",
                                    cursor: "pointer",
                                }}
                            >
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}