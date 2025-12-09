import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Send, Search, MoreVertical } from 'lucide-react';
import { apiClient } from '../../api/client';
import { messagesApi, type ConversationListItem } from '../../api/messages';
import { useAuth } from '../../contexts/AuthContext';
import { Avatar, Button } from '../../components/ui';
import { clsx } from 'clsx';

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
  const navigate = useNavigate();
  const { user } = useAuth();
  const [conversations, setConversations] = useState<ConversationListItem[]>([]);
  const [activeConversation, setActiveConversation] = useState<ConversationResponse | null>(null);
  const [otherUserName, setOtherUserName] = useState<string>('');
  const [otherUserAvatar, setOtherUserAvatar] = useState<string | undefined>();
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadConversations();
  }, []);

  useEffect(() => {
    if (otherUserId) {
      loadConversation(Number(otherUserId));
    }
  }, [otherUserId]);

  const loadConversations = async () => {
    try {
      const convos = await messagesApi.getAllConversations();
      setConversations(convos);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load conversations:', error);
      setLoading(false);
    }
  };

  const loadConversation = async (userId: number) => {
    try {
      const [conversationRes, profileRes] = await Promise.all([
        apiClient.get<ConversationResponse>(`/messages/conversation/${userId}`),
        apiClient.get<{ displayName: string; avatarUrl?: string }>(`/profile/${userId}`),
      ]);

      setActiveConversation(conversationRes);
      setOtherUserName(profileRes.displayName);
      setOtherUserAvatar(profileRes.avatarUrl);

      // Mark messages as read
      await apiClient.post(`/messages/mark-read/${userId}`, {});

      // Update unread count in conversations list
      setConversations(prev =>
        prev.map(conv =>
          conv.otherUserId === userId ? { ...conv, unreadCount: 0 } : conv
        )
      );

      // Dispatch event to update notifications in parent
      window.dispatchEvent(new CustomEvent('messagesRead'));

      // Scroll to bottom
      setTimeout(() => scrollToBottom(), 100);
    } catch (error) {
      console.error('Failed to load conversation:', error);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e: FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !otherUserId) return;

    setSending(true);

    try {
      await apiClient.post('/messages/send', {
        recipientId: Number(otherUserId),
        body: newMessage.trim(),
      });

      setNewMessage('');

      // Reload conversation and conversations list
      await Promise.all([
        loadConversation(Number(otherUserId)),
        loadConversations(),
      ]);
    } catch (error) {
      console.error('Failed to send message:', error);
    } finally {
      setSending(false);
    }
  };

  const filteredConversations = conversations.filter(conv =>
    conv.otherUserName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInMins = Math.floor(diffInMs / 60000);
    const diffInHours = Math.floor(diffInMs / 3600000);

    if (diffInMins < 1) return 'Just now';
    if (diffInMins < 60) return `${diffInMins}m`;
    if (diffInHours < 24) return `${diffInHours}h`;
    return date.toLocaleDateString();
  };

  return (
    <div className="flex h-screen">
      {/* Conversations List */}
      <div className="w-96 border-r border-light-border flex flex-col bg-light-bg">
        {/* Header */}
        <div className="p-4 border-b border-light-border">
          <h2 className="text-xl font-bold text-light-text-primary mb-4">
            Messages
          </h2>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400" />
            <input
              type="text"
              placeholder="Search messages"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input pl-10"
            />
          </div>
        </div>

        {/* Conversations */}
        <div className="flex-1 overflow-y-auto">
          {loading ? (
            <div className="flex items-center justify-center h-32">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
            </div>
          ) : filteredConversations.length === 0 ? (
            <div className="p-8 text-center">
              <p className="text-light-text-secondary">
                {searchQuery ? 'No conversations found' : 'No messages yet'}
              </p>
            </div>
          ) : (
            <div>
              {filteredConversations.map((conv) => (
                <button
                  key={conv.otherUserId}
                  onClick={() => navigate(`/chat/${conv.otherUserId}`)}
                  className={clsx(
                    'w-full p-4 flex items-center gap-3 hover:bg-light-surface transition-colors border-b border-light-border',
                    otherUserId === String(conv.otherUserId) && 'bg-light-surface'
                  )}
                >
                  <div className="relative">
                    <Avatar
                      src={conv.otherUserAvatar ?? undefined}
                      alt={conv.otherUserName}
                      size="md"
                      fallback={conv.otherUserName}
                    />
                    {conv.unreadCount > 0 && (
                      <div className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 rounded-full flex items-center justify-center">
                        <span className="text-white text-xs font-semibold">
                          {conv.unreadCount}
                        </span>
                      </div>
                    )}
                  </div>
                  <div className="flex-1 text-left min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <p className="font-semibold truncate text-light-text-primary">
                        {conv.otherUserName}
                      </p>
                      {conv.lastMessageTime && (
                        <span className="text-xs text-light-text-secondary">
                          {formatTimestamp(conv.lastMessageTime)}
                        </span>
                      )}
                    </div>
                    {conv.lastMessage && (
                      <p className={clsx(
                        'text-sm truncate',
                        conv.unreadCount > 0
                          ? 'text-light-text-primary font-semibold'
                          : 'text-light-text-secondary'
                      )}>
                        {conv.lastMessage}
                      </p>
                    )}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Active Chat */}
      <div className="flex-1 flex flex-col bg-light-bg">
        {!otherUserId ? (
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <div className="w-24 h-24 bg-light-surface rounded-full flex items-center justify-center mx-auto mb-4">
                <Send className="w-12 h-12 text-gray-500 dark:text-gray-400" />
              </div>
              <h3 className="text-xl font-semibold text-light-text-primary mb-2">
                Your Messages
              </h3>
              <p className="text-light-text-secondary">
                Select a conversation to start messaging
              </p>
            </div>
          </div>
        ) : (
          <>
            {/* Chat Header */}
            <div className="p-4 border-b border-light-border flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Avatar
                  src={otherUserAvatar}
                  alt={otherUserName}
                  size="md"
                  fallback={otherUserName}
                />
                <div>
                  <h3 className="font-semibold text-light-text-primary">
                    {otherUserName}
                  </h3>
                  <p className="text-sm text-light-text-secondary">
                    Active now
                  </p>
                </div>
              </div>
              <button className="p-2 hover:bg-light-surface rounded-full">
                <MoreVertical className="w-5 h-5 text-gray-600" />
              </button>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {activeConversation?.messages.length === 0 ? (
                <div className="flex items-center justify-center h-full">
                  <p className="text-light-text-secondary">
                    No messages yet. Start the conversation!
                  </p>
                </div>
              ) : (
                activeConversation?.messages.map((msg) => {
                  const isOutgoing = Number(msg.senderId) === Number(user?.id);

                  return (
                    <div
                      key={msg.id}
                      className={clsx(
                        'flex w-full',
                        isOutgoing ? 'justify-end' : 'justify-start'
                      )}
                    >
                      <div
                        className={clsx(
                          'max-w-[70%] rounded-2xl px-4 py-2',
                          isOutgoing
                            ? 'bg-blue-500 text-white'
                            : 'bg-light-surface text-light-text-primary'
                        )}
                      >
                        <p className="text-sm">{msg.body}</p>
                        <p className={clsx(
                          'text-xs mt-1',
                          isOutgoing ? 'text-blue-100' : 'text-light-text-secondary'
                        )}>
                          {formatTimestamp(msg.sentAt)}
                        </p>
                      </div>
                    </div>
                  );
                })
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Message Input */}
            <div className="p-4 border-t border-light-border">
              <form onSubmit={handleSendMessage} className="flex items-center gap-2">
                <input
                  type="text"
                  placeholder="Type a message..."
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  disabled={sending}
                  className="flex-1 px-4 py-3 bg-light-surface border border-light-border rounded-full text-light-text-primary focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <Button
                  type="submit"
                  disabled={!newMessage.trim() || sending}
                  variant="primary"
                  className="rounded-full w-12 h-12 p-0"
                >
                  <Send className="w-5 h-5" />
                </Button>
              </form>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
