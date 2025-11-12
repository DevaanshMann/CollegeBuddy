import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiClient } from "../../api/client";

type Friend = {
    userId: number;
    displayName: string;
    avatarUrl?: string;
};

type RequestItem = {
    id: number;
    fromUser: Friend;
    toUser: Friend;
};

type ConnectionsResponse = {
    connections: Friend[];
    incomingRequests: RequestItem[];
    outgoingRequests: RequestItem[];
};

export function ConnectionsPage() {
    const [data, setData] = useState<ConnectionsResponse>({
        connections: [],
        incomingRequests: [],
        outgoingRequests: [],
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        void loadConnections();
    }, []);

    async function loadConnections() {
        setLoading(true);
        setError(null);
        setStatus(null);

        try {
            const res = await apiClient.get<any>("/connections");
            setData({
                connections: res.connections ?? [],
                incomingRequests: res.incomingRequests ?? [],
                outgoingRequests: res.outgoingRequests ?? [],
            });
        } catch (err: any) {
            console.error("Load connections error:", err);
            setError(err.message ?? "Failed to load connections");
        } finally {
            setLoading(false);
        }
    }

    async function postAndRefresh(path: string, okMessage: string) {
        setError(null);
        setStatus(null);
        try {
            await apiClient.post(path);
            setStatus(okMessage);
            await loadConnections();
        } catch (err: any) {
            console.error("Connections action error:", err);
            setError(err.message ?? "Action failed");
        }
    }

    if (loading && !data) {
        return <p>Loading connections...</p>;
    }

    return (
        <div>
            <h2>Your Connections</h2>

            <p style={{ marginBottom: "1rem", color: "#9ca3af" }}>
                Friends can message you directly. Incoming and outgoing requests let you manage who
                you connect with.
            </p>

            {status && <p style={{ color: "#22c55e", marginBottom: "0.75rem" }}>{status}</p>}
            {error && <p style={{ color: "red", marginBottom: "0.75rem" }}>{error}</p>}

            {/* Friends */}
            <section style={{ marginBottom: "1.5rem" }}>
                <h3>Friends</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Once someone accepts your request, they’ll appear here. Click “Message” to open a
                    chat.
                </p>
                {data.connections.length === 0 ? (
                    <p>No connections yet.</p>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                        {data.connections.map((f) => (
                            <div
                                key={f.userId}
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "space-between",
                                    padding: "0.6rem 0.75rem",
                                    borderRadius: "0.5rem",
                                    border: "1px solid #374151",
                                }}
                            >
                                <div>{f.displayName}</div>
                                <button onClick={() => navigate(`/chat/${f.userId}`)}>Message</button>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* Incoming */}
            <section style={{ marginBottom: "1.5rem" }}>
                <h3>Incoming Requests</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Classmates who want to connect with you. Accept to become friends, or decline.
                </p>
                {data.incomingRequests.length === 0 ? (
                    <p>No incoming requests.</p>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                        {data.incomingRequests.map((r) => (
                            <div
                                key={r.id}
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "space-between",
                                    padding: "0.6rem 0.75rem",
                                    borderRadius: "0.5rem",
                                    border: "1px solid #374151",
                                }}
                            >
                                <div>{r.fromUser?.displayName ?? "Unknown user"}</div>
                                <div style={{ display: "flex", gap: "0.5rem" }}>
                                    <button
                                        onClick={() =>
                                            postAndRefresh(`/connections/${r.id}/accept`, "Connection accepted")
                                        }
                                    >
                                        Accept
                                    </button>
                                    <button
                                        onClick={() =>
                                            postAndRefresh(
                                                `/connections/${r.id}/decline`,
                                                "Request declined"
                                            )
                                        }
                                    >
                                        Decline
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* Outgoing */}
            <section>
                <h3>Outgoing Requests</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Requests you’ve sent. You can cancel them if you change your mind.
                </p>
                {data.outgoingRequests.length === 0 ? (
                    <p>No outgoing requests.</p>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                        {data.outgoingRequests.map((r) => (
                            <div
                                key={r.id}
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "space-between",
                                    padding: "0.6rem 0.75rem",
                                    borderRadius: "0.5rem",
                                    border: "1px solid #374151",
                                }}
                            >
                                <div>{r.toUser?.displayName ?? "Unknown user"}</div>
                                <button
                                    onClick={() =>
                                        postAndRefresh(`/connections/${r.id}/cancel`, "Request cancelled")
                                    }
                                >
                                    Cancel
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
