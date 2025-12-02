// src/api/account.ts
import { apiClient } from './client';

export const accountApi = {
  /**
   * Permanently delete the current user's account
   */
  deleteAccount: (password: string) =>
    apiClient.del('/account', { password }),
};
