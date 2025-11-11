import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { apiClient } from "../../api/client";

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

export function ChatPage() {
    const { otherUserId } = useParams();
    const [conversation, setConversation] = useState<ConversationResponse | null>(
        null
    );
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [newMessage, setNewMessage] = useState("");
    const [sending, setSending] = useState(false);

    const otherIdNum = otherUserId ? Number(otherUserId) : NaN;

    useEffect(() => {
        if (!otherUserId) {
            setError("No user specified.");
            setLoading(false);
            return;
        }

        async function loadConversation() {
            try {
                const res = await apiClient.get<ConversationResponse>(
                    `/messages/conversation/${otherUserId}`
                );
                setConversation(res);
            } catch (err: any) {
                setError(err.message ?? "Failed to load messages");
            } finally {
                setLoading(false);
            }
        }

        void loadConversation();
    }, [otherUserId]);

    async function handleSend(e: FormEvent) {
        e.preventDefault();
        if (!newMessage.trim() || !otherUserId || Number.isNaN(otherIdNum)) {
            return;
        }

        setSending(true);
        setError(null);

        try {
            await apiClient.post("/messages/send", {
                toUserId: otherIdNum,
                body: newMessage.trim(),
            });

            setNewMessage("");

            // Reload conversation (simple approach for now)
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
            <h2>Chat with user {otherUserId}</h2>

            {error && <p style={{ color: "red" }}>{error}</p>}

            <div
                style={{
                    border: "1px solid #ddd",
                    borderRadius: 8,
                    padding: "0.75rem",
                    maxHeight: 400,
                    overflowY: "auto",
                    marginBottom: "0.75rem",
                }}
            >
                {conversation.messages.length === 0 && <p>No messages yet.</p>}
                {conversation.messages.map((m) => (
                    <div
                        key={m.id}
                        style={{
                            marginBottom: "0.5rem",
                            textAlign: "left",
                        }}
                    >
                        <div
                            style={{
                                display: "inline-block",
                                padding: "0.4rem 0.6rem",
                                borderRadius: 8,
                                background: "#f3f3f3",
                            }}
                        >
                            <div style={{ fontSize: "0.8rem", color: "#555" }}>
                                From #{m.senderId}
                            </div>
                            <div>{m.body}</div>
                            <div style={{ fontSize: "0.7rem", color: "#888" }}>
                                {new Date(m.sentAt).toLocaleString()}
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            <form
                onSubmit={handleSend}
                style={{ display: "flex", gap: "0.5rem", maxWidth: 600 }}
            >
                <input
                    type="text"
                    placeholder="Type a message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    style={{ flex: 1 }}
                />
                <button type="submit" disabled={sending}>
                    {sending ? "Sending..." : "Send"}
                </button>
            </form>
        </div>
    );
}
