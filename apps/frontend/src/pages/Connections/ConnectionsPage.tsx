import { useEffect, useState } from "react";
import { apiClient } from "../../api/client";
import { Link } from "react-router-dom";

type ConnectionSummary = {
    userId: number;
    displayName: string | null;
    avatarUrl: string | null;
};

type ConnectionRequestSummary = {
    requestId: number;
    fromUserId: number;
    toUserId: number;
    fromDisplayName: string | null;
    toDisplayName: string | null;
    status: "PENDING" | "ACCEPTED" | "DECLINED";
};

type ConnectionsResponse = {
    connections: ConnectionSummary[];
    incomingRequests: ConnectionRequestSummary[];
    outgoingRequests: ConnectionRequestSummary[];
};

export function ConnectionsPage() {
    const [data, setData] = useState<ConnectionsResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [message, setMessage] = useState<string | null>(null);

    useEffect(() => {
        async function loadConnections() {
            try {
                const res = await apiClient.get<ConnectionsResponse>("/connections");
                setData(res);
            } catch (err: any) {
                setError(err.message ?? "Failed to load connections");
            } finally {
                setLoading(false);
            }
        }

        void loadConnections();
    }, []);

    async function respondToRequest(requestId: number, action: "ACCEPT" | "DECLINE") {
        setError(null);
        setMessage(null);
        try {
            await apiClient.post("/connections/respond", { requestId, action });
            setMessage(`Request ${action.toLowerCase()}ed`);

            // reload list
            const res = await apiClient.get<ConnectionsResponse>("/connections");
            setData(res);
        } catch (err: any) {
            setError(err.message ?? "Failed to respond to request");
        }
    }

    if (loading) return <p>Loading connections...</p>;

    if (!data) return <p>No connection data.</p>;

    return (
        <div>
            <h2>Your Connections</h2>

            {message && <p style={{ color: "green" }}>{message}</p>}
            {error && <p style={{ color: "red" }}>{error}</p>}

            <section style={{ marginBottom: "1rem" }}>
                <h3>Friends</h3>
                {data.connections.length === 0 && <p>No connections yet.</p>}
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {data.connections.map((c) => (
                        <li
                            key={c.userId}
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: "0.5rem",
                                marginBottom: "0.5rem",
                                display: "flex",
                                alignItems: "center",
                                gap: "0.75rem",
                            }}
                        >
                            {c.avatarUrl && (
                                <img
                                    src={c.avatarUrl}
                                    alt={c.displayName ?? "avatar"}
                                    style={{ width: 32, height: 32, borderRadius: "50%" }}
                                />
                            )}
                            <span style={{ flex: 1 }}>
                {c.displayName ?? `User #${c.userId}`}
              </span>
                            <Link to={`/chat/${c.userId}`}>Open Chat</Link>
                        </li>
                    ))}
                </ul>
            </section>

            <section style={{ marginBottom: "1rem" }}>
                <h3>Incoming Requests</h3>
                {data.incomingRequests.length === 0 && <p>No incoming requests.</p>}
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {data.incomingRequests.map((r) => (
                        <li
                            key={r.requestId}
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: "0.5rem",
                                marginBottom: "0.5rem",
                            }}
                        >
                            <div>
                                From: {r.fromDisplayName ?? `User #${r.fromUserId}`} (
                                {r.status})
                            </div>
                            <div style={{ marginTop: "0.5rem", display: "flex", gap: "0.5rem" }}>
                                <button onClick={() => respondToRequest(r.requestId, "ACCEPT")}>
                                    Accept
                                </button>
                                <button onClick={() => respondToRequest(r.requestId, "DECLINE")}>
                                    Decline
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            </section>

            <section>
                <h3>Outgoing Requests</h3>
                {data.outgoingRequests.length === 0 && <p>No outgoing requests.</p>}
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {data.outgoingRequests.map((r) => (
                        <li
                            key={r.requestId}
                            style={{
                                border: "1px solid #ddd",
                                borderRadius: 8,
                                padding: "0.5rem",
                                marginBottom: "0.5rem",
                            }}
                        >
                            <div>
                                To: {r.toDisplayName ?? `User #${r.toUserId}`} ({r.status})
                            </div>
                        </li>
                    ))}
                </ul>
            </section>
        </div>
    );
}
