// src/api/admin.ts
import { apiClient } from './client';

export type AccountStatus = 'PENDING_VERIFICATION' | 'ACTIVE' | 'DEACTIVATED';
export type Role = 'STUDENT' | 'ADMIN';

export interface AdminUserDto {
  userId: number;
  email: string;
  displayName: string;
  campusDomain: string;
  status: AccountStatus;
  role: Role;
  avatarUrl?: string;
  createdAt?: string;
}

export interface AdminStatsDto {
  totalUsers: number;
  activeUsers: number;
  pendingVerificationUsers: number;
  deactivatedUsers: number;
  totalConnections: number;
  totalMessages: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminApi = {
  /**
   * Get paginated list of all users
   */
  getUsers: (page: number = 0, size: number = 20, sortBy: string = 'id') =>
    apiClient.get<PageResponse<AdminUserDto>>(
      `/admin/users?page=${page}&size=${size}&sortBy=${sortBy}`
    ),

  /**
   * Get detailed information about a specific user
   */
  getUserDetails: (userId: number) =>
    apiClient.get<AdminUserDto>(`/admin/users/${userId}`),

  /**
   * Update user account status
   */
  updateUserStatus: (userId: number, status: AccountStatus) =>
    apiClient.put(`/admin/users/${userId}/status`, { status }),

  /**
   * Get platform statistics
   */
  getStats: () =>
    apiClient.get<AdminStatsDto>('/admin/stats'),
};
