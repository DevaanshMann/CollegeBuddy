import type { FormEvent } from "react";
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { apiClient } from "../../api/client";
import { JWT_STORAGE_KEY } from "../../config";

type Message = {
    id: number;
    senderId: number;
    body: string;
    sentAt: string;
};

type ConversationResponse = {
    conversationId: number;
    messages: Message[];
};

type ProfileDto = {
    displayName: string;
    bio: string;
    avatarUrl: string;
    visibility: string;
};

export function ChatPage() {
    const { otherUserId } = useParams();
    const [conversation, setConversation] = useState<ConversationResponse | null>(
        null
    );
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [newMessage, setNewMessage] = useState("");
    const [sending, setSending] = useState(false);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);
    const [otherUserProfile, setOtherUserProfile] = useState<ProfileDto | null>(null);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const otherIdNum = otherUserId ? Number(otherUserId) : NaN;

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

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        // Get current user ID
        const userId = getUserIdFromToken();
        if (userId) {
            setCurrentUserId(Number(userId));
        }

        if (!otherUserId) {
            setError("No user specified.");
            setLoading(false);
            return;
        }

        async function loadData() {
            try {
                // Load conversation and other user's profile in parallel
                const [conversationRes, profileRes] = await Promise.all([
                    apiClient.get<ConversationResponse>(`/messages/conversation/${otherUserId}`),
                    apiClient.get<ProfileDto>(`/profile/${otherUserId}`)
                ]);
                setConversation(conversationRes);
                setOtherUserProfile(profileRes);
            } catch (err: any) {
                setError(err.message ?? "Failed to load data");
            } finally {
                setLoading(false);
            }
        }

        void loadData();
    }, [otherUserId]);

    // Scroll to bottom when messages change
    useEffect(() => {
        scrollToBottom();
    }, [conversation?.messages]);

    async function handleSend(e: FormEvent) {
        e.preventDefault();
        if (!newMessage.trim() || !otherUserId || Number.isNaN(otherIdNum)) {
            return;
        }

        setSending(true);
        setError(null);

        try {
            // Fixed: Use recipientId instead of toUserId
            await apiClient.post("/messages/send", {
                recipientId: otherIdNum,
                body: newMessage.trim(),
            });

            setNewMessage("");

            // Reload conversation
            const res = await apiClient.get<ConversationResponse>(
                `/messages/conversation/${otherUserId}`
            );
            setConversation(res);
        } catch (err: any) {
            setError(err.message ?? "Failed to send message");
        } finally {
            setSending(false);
        }
    }

    if (loading) return <p>Loading conversation...</p>;
    if (!conversation) return <p>No conversation found.</p>;

    return (
        <div>
            <h2 style={{ marginBottom: "1rem" }}>{otherUserProfile?.displayName || `User ${otherUserId}`}</h2>

            {error && <p style={{ color: "red" }}>{error}</p>}

            <div
                style={{
                    border: "1px solid #ddd",
                    borderRadius: 8,
                    backgroundColor: "#f9f9f9",
                    display: "flex",
                    flexDirection: "column",
                    height: 500,
                }}
            >
                {/* Messages area */}
                <div
                    style={{
                        flex: 1,
                        overflowY: "auto",
                        padding: "0.75rem",
                    }}
                >
                    {conversation.messages.length === 0 && <p>No messages yet. Start the conversation!</p>}
                    {conversation.messages.map((m) => {
                        const isOutgoing = m.senderId === currentUserId;
                        const senderName = isOutgoing ? "You" : (otherUserProfile?.displayName || "User");

                        return (
                            <div
                                key={m.id}
                                style={{
                                    marginBottom: "0.5rem",
                                    textAlign: isOutgoing ? "right" : "left",
                                }}
                            >
                                <div
                                    style={{
                                        display: "inline-block",
                                        padding: "0.4rem 0.6rem",
                                        borderRadius: 8,
                                        background: isOutgoing ? "#007bff" : "#e3f2fd",
                                        color: isOutgoing ? "#fff" : "#000",
                                        maxWidth: "70%",
                                        textAlign: "left",
                                    }}
                                >
                                    <div style={{
                                        fontSize: "0.8rem",
                                        fontWeight: "bold",
                                        color: isOutgoing ? "#e3f2fd" : "#555",
                                        marginBottom: "0.2rem"
                                    }}>
                                        {senderName}
                                    </div>
                                    <div>{m.body}</div>
                                    <div style={{
                                        fontSize: "0.7rem",
                                        color: isOutgoing ? "#cce5ff" : "#888",
                                        marginTop: "0.2rem"
                                    }}>
                                        {new Date(m.sentAt).toLocaleString()}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                    <div ref={messagesEndRef} />
                </div>

                {/* Input area */}
                <form
                    onSubmit={handleSend}
                    style={{
                        display: "flex",
                        gap: "0.5rem",
                        padding: "0.75rem",
                        borderTop: "1px solid #ddd",
                        backgroundColor: "#fff",
                        borderRadius: "0 0 8px 8px",
                    }}
                >
                    <input
                        type="text"
                        placeholder="Type a message..."
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        style={{
                            flex: 1,
                            padding: "0.5rem",
                            border: "1px solid #ddd",
                            borderRadius: 4,
                            outline: "none",
                        }}
                    />
                    <button
                        type="submit"
                        disabled={sending}
                        style={{
                            padding: "0.5rem 1rem",
                            backgroundColor: "#007bff",
                            color: "#fff",
                            border: "none",
                            borderRadius: 4,
                            cursor: sending ? "not-allowed" : "pointer",
                            opacity: sending ? 0.6 : 1,
                        }}
                    >
                        {sending ? "Sending..." : "Send"}
                    </button>
                </form>
            </div>
        </div>
    );
}