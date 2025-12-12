import { useState, useEffect, useRef } from 'react';
import type { FormEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Send, ArrowLeft, Users } from 'lucide-react';
import { groupsApi } from '../../api/groups';
import type { GroupDto, GroupMessageDto, SendGroupMessageRequest } from '../../api/groups';
import { useAuth } from '../../contexts/AuthContext';
import { Avatar, Button } from '../../components/ui';
import { clsx } from 'clsx';
import toast from 'react-hot-toast';

export function GroupChatPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [group, setGroup] = useState<GroupDto | null>(null);
  const [messages, setMessages] = useState<GroupMessageDto[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (groupId) {
      loadGroupData();
    }
  }, [groupId]);

  const loadGroupData = async () => {
    setLoading(true);
    try {
      const [groupData, messagesData] = await Promise.all([
        groupsApi.getGroupDetails(Number(groupId)),
        groupsApi.getGroupMessages(Number(groupId)),
      ]);

      setGroup(groupData);
      setMessages(messagesData);

      // Mark messages as read
      await groupsApi.markGroupAsRead(Number(groupId));

      // Dispatch event to update notifications in parent (App.tsx)
      window.dispatchEvent(new CustomEvent('messagesRead'));

      // Scroll to bottom
      setTimeout(() => scrollToBottom(), 100);
    } catch (error: any) {
      console.error('Failed to load group data:', error);
      toast.error(error.message ?? 'Failed to load group chat');
    } finally {
      setLoading(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e: FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !groupId) return;

    setSending(true);

    try {
      const request: SendGroupMessageRequest = {
        body: newMessage.trim(),
      };

      const sentMessage = await groupsApi.sendGroupMessage(Number(groupId), request);

      setMessages(prev => [...prev, sentMessage]);
      setNewMessage('');

      setTimeout(() => scrollToBottom(), 100);
    } catch (error: any) {
      console.error('Failed to send message:', error);
      toast.error(error.message ?? 'Failed to send message');
    } finally {
      setSending(false);
    }
  };

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!group) {
    return (
      <div className="h-screen flex flex-col items-center justify-center">
        <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-4">
          Group not found
        </h3>
        <Button variant="primary" onClick={() => navigate('/groups')}>
          Back to Groups
        </Button>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-light-bg dark:text-dark-bg">
      {/* Header */}
      <div className="bg-light-surface dark:bg-dark-surface border-b border-light-border dark:border-dark-border p-4">
        <div className="max-w-5xl mx-auto flex items-center gap-4">
          <button
            onClick={() => navigate(`/groups/${groupId}`)}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-colors"
          >
            <ArrowLeft className="w-5 h-5 text-light-text-primary dark:text-dark-text-primary" />
          </button>

          <div className="flex-1 min-w-0">
            <h2 className="font-semibold text-light-text-primary dark:text-dark-text-primary truncate">
              {group.name}
            </h2>
            <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
              {group.memberCount} {group.memberCount === 1 ? 'member' : 'members'}
            </p>
          </div>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4">
        <div className="max-w-5xl mx-auto space-y-4">
          {messages.length === 0 ? (
            <div className="text-center py-12">
              <div className="w-16 h-16 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
                <Users className="w-8 h-8 text-gray-500 dark:text-gray-400" />
              </div>
              <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
                No messages yet
              </h3>
              <p className="text-light-text-secondary dark:text-dark-text-secondary">
                Be the first to send a message to this group!
              </p>
            </div>
          ) : (
            messages.map((message) => {
              const isOwn = Number(user?.id) === Number(message.senderId);

              return (
                <div
                  key={message.id}
                  className={clsx(
                    'flex w-full gap-2',
                    isOwn ? 'justify-end' : 'justify-start'
                  )}
                >
                  {/* Avatar for incoming messages only */}
                  {!isOwn && (
                    <Avatar
                      src={message.senderAvatar}
                      alt={message.senderName}
                      size="sm"
                      fallback={message.senderName}
                      className="flex-shrink-0 mt-1"
                    />
                  )}

                  {/* Message bubble */}
                  <div className="flex flex-col max-w-[70%]">
                    {!isOwn && (
                      <span className="text-xs text-light-text-secondary dark:text-dark-text-secondary mb-1 ml-2">
                        {message.senderName}
                      </span>
                    )}
                    <div
                      className={clsx(
                        'rounded-2xl px-4 py-2 break-words',
                        isOwn
                          ? 'bg-blue-500 text-white'
                          : 'bg-light-surface dark:bg-dark-surface text-light-text-primary dark:text-dark-text-primary'
                      )}
                    >
                      <p className="text-sm whitespace-pre-wrap">{message.body}</p>
                      <p className={clsx(
                        'text-xs mt-1',
                        isOwn ? 'text-blue-100' : 'text-light-text-secondary dark:text-dark-text-secondary'
                      )}>
                        {new Date(message.sentAt).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Message Input */}
      <div className="bg-light-surface dark:bg-dark-surface border-t border-light-border dark:border-dark-border p-4">
        <form onSubmit={handleSendMessage} className="max-w-5xl mx-auto">
          <div className="flex gap-2">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
              disabled={sending}
              className="flex-1 px-4 py-3 bg-light-bg dark:bg-dark-bg border border-light-border dark:border-dark-border rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary disabled:opacity-50"
              autoFocus
            />
            <Button
              type="submit"
              variant="primary"
              loading={sending}
              disabled={!newMessage.trim()}
              className="rounded-full w-12 h-12 p-0 flex items-center justify-center"
            >
              <Send className="w-5 h-5" />
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
