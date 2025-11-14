import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiClient } from "../../api/client";

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
            const res = await apiClient.get<ConnectionsResponse>("/connections");
            setData(res);
        } catch (err: any) {
            console.error("Load connections error:", err);
            setError(err.message ?? "Failed to load connections");
        } finally {
            setLoading(false);
        }
    }

    async function handleRespond(userId: number, decision: string) {
        setError(null);
        setStatus(null);
        try {
            await apiClient.post("/connections/respond", {
                requestId: userId,
                decision: decision,
            });
            setStatus(`Request ${decision.toLowerCase()}ed!`);
            await loadConnections();
        } catch (err: any) {
            console.error("Respond error:", err);
            setError(err.message ?? "Action failed");
        }
    }

    if (loading) {
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
                <h3>Friends ({data.connections.length})</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Click "Message" to open a chat with your connection.
                </p>
                {data.connections.length === 0 ? (
                    <p>No connections yet. Search for classmates to connect!</p>
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
                                    backgroundColor: "#f9f9f9",
                                }}
                            >
                                <div>
                                    <strong>{f.displayName}</strong>
                                    {f.campusDomain && (
                                        <span style={{ fontSize: 12, color: "#666", marginLeft: "0.5rem" }}>
                                            @{f.campusDomain}
                                        </span>
                                    )}
                                </div>
                                <button onClick={() => navigate(`/chat/${f.userId}`)}>Message</button>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* Incoming */}
            <section style={{ marginBottom: "1.5rem" }}>
                <h3>Incoming Requests ({data.incomingRequests.length})</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Classmates who want to connect with you.
                </p>
                {data.incomingRequests.length === 0 ? (
                    <p>No incoming requests.</p>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                        {data.incomingRequests.map((r) => (
                            <div
                                key={r.userId}
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "space-between",
                                    padding: "0.6rem 0.75rem",
                                    borderRadius: "0.5rem",
                                    border: "1px solid #374151",
                                    backgroundColor: "#fff3cd",
                                }}
                            >
                                <div>{r.displayName}</div>
                                <div style={{ display: "flex", gap: "0.5rem" }}>
                                    <button onClick={() => handleRespond(r.userId, "ACCEPT")}>
                                        Accept
                                    </button>
                                    <button onClick={() => handleRespond(r.userId, "DECLINE")}>
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
                <h3>Outgoing Requests ({data.outgoingRequests.length})</h3>
                <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                    Requests you've sent waiting for response.
                </p>
                {data.outgoingRequests.length === 0 ? (
                    <p>No outgoing requests.</p>
                ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                        {data.outgoingRequests.map((r) => (
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
                                <div>{r.displayName}</div>
                                <span style={{ fontSize: 12, color: "#666" }}>Pending...</span>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}