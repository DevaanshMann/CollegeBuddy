// src/api/blocking.ts
import { apiClient } from './client';

export interface BlockedUserDto {
  id: number;
  userId: number;
  displayName: string;
  avatarUrl?: string;
  blockedAt: string;
}

export const blockingApi = {
  /**
   * Block a user
   */
  blockUser: (userId: number) =>
    apiClient.post('/blocked-users', { userId }),

  /**
   * Unblock a user
   */
  unblockUser: (userId: number) =>
    apiClient.del(`/blocked-users/${userId}`),

  /**
   * Get list of blocked users
   */
  getBlockedUsers: () =>
    apiClient.get<BlockedUserDto[]>('/blocked-users'),

  /**
   * Check if a user is blocked
   */
  isBlocked: (userId: number) =>
    apiClient.get<boolean>(`/blocked-users/check/${userId}`),
};
