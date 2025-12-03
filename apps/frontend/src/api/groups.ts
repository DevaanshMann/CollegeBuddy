// src/api/groups.ts
import { apiClient } from './client';

export type Visibility = 'PUBLIC' | 'PRIVATE';
export type GroupRole = 'MEMBER' | 'ADMIN';

export interface GroupDto {
  id: number;
  name: string;
  description?: string;
  campusDomain: string;
  creatorId: number;
  creatorName: string;
  visibility: Visibility;
  memberCount: number;
  isMember: boolean;
  isAdmin: boolean;
  createdAt: string;
}

export interface GroupMemberDto {
  userId: number;
  displayName: string;
  avatarUrl?: string;
  role: GroupRole;
  joinedAt: string;
}

export interface CreateGroupRequest {
  name: string;
  description?: string;
  visibility: Visibility;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface GroupMessageDto {
  id: number;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  body: string;
  sentAt: string;
}

export interface SendGroupMessageRequest {
  body: string;
}

export const groupsApi = {
  /**
   * Create a new group
   */
  createGroup: (request: CreateGroupRequest) =>
    apiClient.post<GroupDto>('/groups', request),

  /**
   * Get all groups for user's campus
   */
  getGroups: (page: number = 0, size: number = 20, search?: string) => {
    const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
    return apiClient.get<PageResponse<GroupDto>>(
      `/groups?page=${page}&size=${size}${searchParam}`
    );
  },

  /**
   * Get group details
   */
  getGroupDetails: (groupId: number) =>
    apiClient.get<GroupDto>(`/groups/${groupId}`),

  /**
   * Get group members
   */
  getGroupMembers: (groupId: number) =>
    apiClient.get<GroupMemberDto[]>(`/groups/${groupId}/members`),

  /**
   * Join a group
   */
  joinGroup: (groupId: number) =>
    apiClient.post(`/groups/${groupId}/join`),

  /**
   * Leave a group
   */
  leaveGroup: (groupId: number) =>
    apiClient.post(`/groups/${groupId}/leave`),

  /**
   * Get group messages
   */
  getGroupMessages: (groupId: number) =>
    apiClient.get<GroupMessageDto[]>(`/groups/${groupId}/messages`),

  /**
   * Send a message to a group
   */
  sendGroupMessage: (groupId: number, request: SendGroupMessageRequest) =>
    apiClient.post<GroupMessageDto>(`/groups/${groupId}/messages`, request),
};
