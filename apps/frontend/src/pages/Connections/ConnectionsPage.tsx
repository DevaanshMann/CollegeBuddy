import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { apiClient } from "../../api/client";
import { blockingApi } from "../../api/blocking";
import toast from "react-hot-toast";

type UserDto = {
    id: number;
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
    const [searchParams] = useSearchParams();
    const view = searchParams.get('view'); // 'connections' or 'requests'

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
    const [confirmBlock, setConfirmBlock] = useState<{ userId: number; displayName: string } | null>(null);
    const [blockedUsers, setBlockedUsers] = useState<number[]>([]);

    // All sections start collapsed
    const [friendsOpen, setFriendsOpen] = useState(false);
    const [incomingOpen, setIncomingOpen] = useState(false);
    const [outgoingOpen, setOutgoingOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        void loadConnections();
        void loadBlockedUsers();
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

    async function loadBlockedUsers() {
        try {
            const blocked = await blockingApi.getBlockedUsers();
            setBlockedUsers(blocked.map((b) => b.userId));
        } catch (err: any) {
            console.error("Failed to load blocked users:", err);
        }
    }

    function showBlockConfirm(userId: number, displayName: string) {
        setConfirmBlock({ userId, displayName });
    }

    async function handleBlock() {
        if (!confirmBlock) return;

        try {
            await blockingApi.blockUser(confirmBlock.userId);
            toast.success(`Blocked ${confirmBlock.displayName}. Connection removed.`);
            setConfirmBlock(null);
            await loadConnections();
            await loadBlockedUsers();
        } catch (err: any) {
            console.error("Block error:", err);
            toast.error(err.message ?? "Failed to block user");
            setConfirmBlock(null);
        }
    }

    async function handleUnblock(userId: number, displayName: string) {
        try {
            await blockingApi.unblockUser(userId);
            toast.success(`Unblocked ${displayName}`);
            await loadBlockedUsers();
        } catch (err: any) {
            console.error("Unblock error:", err);
            toast.error(err.message ?? "Failed to unblock user");
        }
    }

    if (loading) {
        return <p className="text-light-text-primary dark:text-dark-text-primary">Loading connections...</p>;
    }

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <h2 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2">Your Connections</h2>

            <p className="mb-4 text-light-text-secondary dark:text-dark-text-secondary">
                Friends can message you directly. Incoming and outgoing requests let you manage who
                you connect with.
            </p>

            {status && <p className="text-green-500 mb-3">{status}</p>}
            {error && <p className="text-red-500 mb-3">{error}</p>}

            {/* Friends */}
            {view !== 'requests' && (
            <section className="mb-6">
                {(() => {
                    const totalUnread = Object.values(data.unreadCounts).reduce((sum, count) => sum + count, 0);
                    return (
                        <div
                            onClick={() => setFriendsOpen(!friendsOpen)}
                            className={`flex items-center justify-between cursor-pointer p-4 rounded-lg ${
                                totalUnread > 0
                                    ? 'bg-amber-50 dark:bg-amber-900/20 border-2 border-amber-500'
                                    : 'bg-green-50 dark:bg-green-900/20 border border-green-500'
                            } ${friendsOpen ? 'mb-3' : ''}`}
                        >
                            <div className="flex items-center gap-3">
                                <h3 className="m-0 font-semibold text-light-text-primary dark:text-dark-text-primary">
                                    Friends ({data.connections.length})
                                </h3>
                                {totalUnread > 0 && (
                                    <span className="bg-red-500 text-white rounded-full px-2.5 py-1 text-sm font-bold">
                                        {totalUnread} new message{totalUnread > 1 ? "s" : ""}
                                    </span>
                                )}
                            </div>
                            <span className="text-xl text-light-text-primary dark:text-dark-text-primary">
                                {friendsOpen ? "▼" : "▶"}
                            </span>
                        </div>
                    );
                })()}
                {friendsOpen && (
                    <>
                        <p className="text-light-text-secondary dark:text-dark-text-secondary mb-2 text-sm">
                            Click "Message" to open a chat with your connection.
                        </p>
                        {data.connections.length === 0 ? (
                            <p className="text-light-text-secondary dark:text-dark-text-secondary">
                                No connections yet. Search for classmates to connect!
                            </p>
                        ) : (
                            <div className="flex flex-col gap-2">
                                {data.connections.map((f) => {
                                    const unreadCount = data.unreadCounts[f.id] || 0;
                                    return (
                                        <div
                                            key={f.id}
                                            className={`flex items-center justify-between p-3 rounded-lg ${
                                                unreadCount > 0
                                                    ? 'bg-amber-50 dark:bg-amber-900/20 border-2 border-amber-500'
                                                    : 'bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border'
                                            }`}
                                        >
                                            <div className="flex items-center gap-2">
                                                <strong className="text-light-text-primary dark:text-dark-text-primary">
                                                    {f.displayName}
                                                </strong>
                                                {f.campusDomain && (
                                                    <span className="text-xs text-light-text-secondary dark:text-dark-text-secondary">
                                                        @{f.campusDomain}
                                                    </span>
                                                )}
                                                {unreadCount > 0 && (
                                                    <span className="bg-red-500 text-white rounded-full px-2 py-0.5 text-xs font-bold ml-1">
                                                        {unreadCount}
                                                    </span>
                                                )}
                                            </div>
                                            <div className="flex gap-2">
                                                {blockedUsers.includes(f.id) ? (
                                                    <button
                                                        onClick={() => handleUnblock(f.id, f.displayName)}
                                                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                                                    >
                                                        Unblock
                                                    </button>
                                                ) : (
                                                    <>
                                                        <button
                                                            onClick={() => navigate(`/chat/${f.id}`)}
                                                            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                                                        >
                                                            Message
                                                        </button>
                                                        <button
                                                            onClick={() => showBlockConfirm(f.id, f.displayName)}
                                                            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                                                        >
                                                            Block
                                                        </button>
                                                        <button
                                                            onClick={() => showDisconnectConfirm(f.id, f.displayName)}
                                                            className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                                                        >
                                                            Disconnect
                                                        </button>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </>
                )}
            </section>
            )}

            {/* Incoming */}
            {view !== 'connections' && (
            <section className="mb-6">
                <div
                    onClick={() => setIncomingOpen(!incomingOpen)}
                    className={`flex items-center justify-between cursor-pointer p-4 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-amber-500 ${
                        incomingOpen ? 'mb-3' : ''
                    }`}
                >
                    <h3 className="m-0 font-semibold text-light-text-primary dark:text-dark-text-primary">
                        Incoming Requests ({data.incomingRequests.length})
                    </h3>
                    <span className="text-xl text-light-text-primary dark:text-dark-text-primary">
                        {incomingOpen ? "▼" : "▶"}
                    </span>
                </div>
                {incomingOpen && (
                    <>
                        <p className="text-light-text-secondary dark:text-dark-text-secondary mb-2 text-sm">
                            Classmates who want to connect with you.
                        </p>
                        {data.incomingRequests.length === 0 ? (
                            <p className="text-light-text-secondary dark:text-dark-text-secondary">
                                No incoming requests.
                            </p>
                        ) : (
                            <div className="flex flex-col gap-2">
                                {data.incomingRequests.map((r) => (
                                    <div
                                        key={r.userId}
                                        className="flex items-center justify-between p-3 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-light-border dark:border-dark-border"
                                    >
                                        <div className="text-light-text-primary dark:text-dark-text-primary">
                                            {r.displayName}
                                        </div>
                                        <div className="flex gap-2">
                                            <button
                                                onClick={() => handleRespond(r.requestId, "ACCEPT")}
                                                className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                                            >
                                                Accept
                                            </button>
                                            <button
                                                onClick={() => handleRespond(r.requestId, "DECLINE")}
                                                className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                                            >
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
            )}

            {/* Outgoing */}
            {!view && (
            <section>
                <div
                    onClick={() => setOutgoingOpen(!outgoingOpen)}
                    className={`flex items-center justify-between cursor-pointer p-4 rounded-lg bg-indigo-50 dark:bg-indigo-900/20 border border-indigo-500 ${
                        outgoingOpen ? 'mb-3' : ''
                    }`}
                >
                    <h3 className="m-0 font-semibold text-light-text-primary dark:text-dark-text-primary">
                        Outgoing Requests ({data.outgoingRequests.length})
                    </h3>
                    <span className="text-xl text-light-text-primary dark:text-dark-text-primary">
                        {outgoingOpen ? "▼" : "▶"}
                    </span>
                </div>
                {outgoingOpen && (
                    <>
                        <p className="text-light-text-secondary dark:text-dark-text-secondary mb-2 text-sm">
                            Requests you've sent waiting for response.
                        </p>
                        {data.outgoingRequests.length === 0 ? (
                            <p className="text-light-text-secondary dark:text-dark-text-secondary">
                                No outgoing requests.
                            </p>
                        ) : (
                            <div className="flex flex-col gap-2">
                                {data.outgoingRequests.map((r) => (
                                    <div
                                        key={r.userId}
                                        className="flex items-center justify-between p-3 rounded-lg bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border"
                                    >
                                        <div className="text-light-text-primary dark:text-dark-text-primary">
                                            {r.displayName}
                                        </div>
                                        <span className="text-xs text-light-text-secondary dark:text-dark-text-secondary">
                                            Pending...
                                        </span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </section>
            )}

            {/* Disconnect Confirmation Modal */}
            {confirmDisconnect && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-light-bg dark:bg-dark-bg p-8 rounded-lg max-w-md text-center shadow-xl">
                        <h3 className="text-xl font-semibold mb-4 text-light-text-primary dark:text-dark-text-primary">
                            Confirm Disconnect
                        </h3>
                        <p className="mb-6 text-light-text-secondary dark:text-dark-text-secondary">
                            Are you sure you want to disconnect from{' '}
                            <strong className="text-light-text-primary dark:text-dark-text-primary">
                                {confirmDisconnect.displayName}
                            </strong>
                            ? You will need to send a new connection request to reconnect.
                        </p>
                        <div className="flex gap-4 justify-center">
                            <button
                                onClick={() => setConfirmDisconnect(null)}
                                className="px-6 py-2 rounded border border-light-border dark:border-dark-border bg-light-bg dark:bg-dark-bg text-light-text-primary dark:text-dark-text-primary hover:bg-light-surface dark:hover:bg-dark-surface"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDisconnect}
                                className="px-6 py-2 rounded bg-red-600 text-white hover:bg-red-700"
                            >
                                Confirm
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Block Confirmation Modal */}
            {confirmBlock && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-light-bg dark:bg-dark-bg p-8 rounded-lg max-w-md text-center shadow-xl">
                        <h3 className="text-xl font-semibold mb-4 text-light-text-primary dark:text-dark-text-primary">
                            Confirm Block
                        </h3>
                        <p className="mb-6 text-light-text-secondary dark:text-dark-text-secondary">
                            Are you sure you want to block{' '}
                            <strong className="text-light-text-primary dark:text-dark-text-primary">
                                {confirmBlock.displayName}
                            </strong>
                            ? This will remove your connection and prevent them from contacting you.
                        </p>
                        <div className="flex gap-4 justify-center">
                            <button
                                onClick={() => setConfirmBlock(null)}
                                className="px-6 py-2 rounded border border-light-border dark:border-dark-border bg-light-bg dark:bg-dark-bg text-light-text-primary dark:text-dark-text-primary hover:bg-light-surface dark:hover:bg-dark-surface"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleBlock}
                                className="px-6 py-2 rounded bg-gray-600 text-white hover:bg-gray-700"
                            >
                                Block User
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}