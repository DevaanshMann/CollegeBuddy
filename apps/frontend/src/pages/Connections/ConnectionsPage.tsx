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

type ConnectionRequestDto = {
    requestId: number;
    userId: number;
    displayName: string;
    avatarUrl?: string;
    visibility?: string;
    campusDomain?: string;
};

type ConnectionsResponse = {
    connections: UserDto[];
    incomingRequests: ConnectionRequestDto[];
    outgoingRequests: ConnectionRequestDto[];
    unreadCounts: Record<number, number>;
};

export function ConnectionsPage() {
    const [data, setData] = useState<ConnectionsResponse>({
        connections: [],
        incomingRequests: [],
        outgoingRequests: [],
        unreadCounts: {},
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string | null>(null);
    const [confirmDisconnect, setConfirmDisconnect] = useState<{ userId: number; displayName: string } | null>(null);
    const [friendsOpen, setFriendsOpen] = useState(false);
    const [incomingOpen, setIncomingOpen] = useState(false);
    const [outgoingOpen, setOutgoingOpen] = useState(false);
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

    async function handleRespond(requestId: number, decision: string) {
        setError(null);
        setStatus(null);
        try {
            await apiClient.post("/connections/respond", {
                requestId: requestId,
                decision: decision,
            });
            setStatus(`Request ${decision.toLowerCase()}ed!`);
            await loadConnections();
        } catch (err: any) {
            console.error("Respond error:", err);
            setError(err.message ?? "Action failed");
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
            await loadConnections();
        } catch (err: any) {
            console.error("Disconnect error:", err);
            setError(err.message ?? "Failed to disconnect");
            setConfirmDisconnect(null);
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
                {(() => {
                    const totalUnread = Object.values(data.unreadCounts).reduce((sum, count) => sum + count, 0);
                    return (
                        <div
                            onClick={() => setFriendsOpen(!friendsOpen)}
                            style={{
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                                cursor: "pointer",
                                padding: "0.75rem 1rem",
                                backgroundColor: totalUnread > 0 ? "#fef3c7" : "#e8f4e8",
                                borderRadius: "0.5rem",
                                border: totalUnread > 0 ? "2px solid #f59e0b" : "1px solid #4ade80",
                                marginBottom: friendsOpen ? "0.75rem" : 0,
                            }}
                        >
                            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                                <h3 style={{ margin: 0 }}>Friends ({data.connections.length})</h3>
                                {totalUnread > 0 && (
                                    <span style={{
                                        backgroundColor: "#ef4444",
                                        color: "white",
                                        borderRadius: "999px",
                                        padding: "0.2rem 0.6rem",
                                        fontSize: "0.9rem",
                                        fontWeight: "bold",
                                    }}>
                                        {totalUnread} new message{totalUnread > 1 ? "s" : ""}
                                    </span>
                                )}
                            </div>
                            <span style={{ fontSize: "1.2rem" }}>{friendsOpen ? "▼" : "▶"}</span>
                        </div>
                    );
                })()}
                {friendsOpen && (
                    <>
                        <p style={{ color: "#9ca3af", marginBottom: "0.5rem", fontSize: 14 }}>
                            Click "Message" to open a chat with your connection.
                        </p>
                        {data.connections.length === 0 ? (
                            <p>No connections yet. Search for classmates to connect!</p>
                        ) : (
                            <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                                {data.connections.map((f) => {
                                    const unreadCount = data.unreadCounts[f.userId] || 0;
                                    return (
                                        <div
                                            key={f.userId}
                                            style={{
                                                display: "flex",
                                                alignItems: "center",
                                                justifyContent: "space-between",
                                                padding: "0.6rem 0.75rem",
                                                borderRadius: "0.5rem",
                                                border: unreadCount > 0 ? "2px solid #f59e0b" : "1px solid #374151",
                                                backgroundColor: unreadCount > 0 ? "#fef3c7" : "#f9f9f9",
                                            }}
                                        >
                                            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                                                <strong>{f.displayName}</strong>
                                                {f.campusDomain && (
                                                    <span style={{ fontSize: 12, color: "#666" }}>
                                                        @{f.campusDomain}
                                                    </span>
                                                )}
                                                {unreadCount > 0 && (
                                                    <span style={{
                                                        backgroundColor: "#ef4444",
                                                        color: "white",
                                                        borderRadius: "999px",
                                                        padding: "0.15rem 0.5rem",
                                                        fontSize: "0.8rem",
                                                        fontWeight: "bold",
                                                        marginLeft: "0.25rem",
                                                    }}>
                                                        {unreadCount}
                                                    </span>
                                                )}
                                            </div>
                                            <div style={{ display: "flex", gap: "0.5rem" }}>
                                                <button
                                                    onClick={() => navigate(`/chat/${f.userId}`)}
                                                    style={{ fontFamily: "inherit" }}
                                                >
                                                    Message
                                                </button>
                                                <button
                                                    onClick={() => showDisconnectConfirm(f.userId, f.displayName)}
                                                    style={{
                                                        backgroundColor: "#dc2626",
                                                        color: "white",
                                                        border: "none",
                                                        padding: "0.5rem 1rem",
                                                        borderRadius: "4px",
                                                        cursor: "pointer",
                                                        fontFamily: "inherit"
                                                    }}
                                                >
                                                    Disconnect
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </>
                )}
            </section>

            {/* Incoming */}
            <section style={{ marginBottom: "1.5rem" }}>
                <div
                    onClick={() => setIncomingOpen(!incomingOpen)}
                    style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        cursor: "pointer",
                        padding: "0.75rem 1rem",
                        backgroundColor: "#fef3c7",
                        borderRadius: "0.5rem",
                        border: "1px solid #fbbf24",
                        marginBottom: incomingOpen ? "0.75rem" : 0,
                    }}
                >
                    <h3 style={{ margin: 0 }}>Incoming Requests ({data.incomingRequests.length})</h3>
                    <span style={{ fontSize: "1.2rem" }}>{incomingOpen ? "▼" : "▶"}</span>
                </div>
                {incomingOpen && (
                    <>
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
                                            <button onClick={() => handleRespond(r.requestId, "ACCEPT")}>
                                                Accept
                                            </button>
                                            <button onClick={() => handleRespond(r.requestId, "DECLINE")}>
                                                Decline
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </section>

            {/* Outgoing */}
            <section>
                <div
                    onClick={() => setOutgoingOpen(!outgoingOpen)}
                    style={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        cursor: "pointer",
                        padding: "0.75rem 1rem",
                        backgroundColor: "#e0e7ff",
                        borderRadius: "0.5rem",
                        border: "1px solid #818cf8",
                        marginBottom: outgoingOpen ? "0.75rem" : 0,
                    }}
                >
                    <h3 style={{ margin: 0 }}>Outgoing Requests ({data.outgoingRequests.length})</h3>
                    <span style={{ fontSize: "1.2rem" }}>{outgoingOpen ? "▼" : "▶"}</span>
                </div>
                {outgoingOpen && (
                    <>
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
                    </>
                )}
            </section>

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