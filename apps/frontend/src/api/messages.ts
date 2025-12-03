import { apiClient } from './client';

export interface ConversationListItem {
  otherUserId: number;
  otherUserName: string;
  otherUserAvatar: string | null;
  lastMessage: string;
  lastMessageTime: string | null;
  unreadCount: number;
}

export const messagesApi = {
  getAllConversations: async (): Promise<ConversationListItem[]> => {
    return apiClient.get<ConversationListItem[]>('/messages/conversations');
  },
};
