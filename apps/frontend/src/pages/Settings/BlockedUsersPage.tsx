import { useEffect, useState } from 'react';
import { UserX, ShieldOff } from 'lucide-react';
import { blockingApi } from '../../api/blocking';
import type { BlockedUserDto } from '../../api/blocking';
import { Avatar, Button, Modal } from '../../components/ui';
import toast from 'react-hot-toast';

export function BlockedUsersPage() {
  const [blockedUsers, setBlockedUsers] = useState<BlockedUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [confirmUnblock, setConfirmUnblock] = useState<BlockedUserDto | null>(null);

  useEffect(() => {
    loadBlockedUsers();
  }, []);

  async function loadBlockedUsers() {
    setLoading(true);
    try {
      const users = await blockingApi.getBlockedUsers();
      setBlockedUsers(users);
    } catch (err: any) {
      console.error('Failed to load blocked users:', err);
      toast.error(err.message ?? 'Failed to load blocked users');
    } finally {
      setLoading(false);
    }
  }

  async function handleUnblock() {
    if (!confirmUnblock) return;

    try {
      await blockingApi.unblockUser(confirmUnblock.userId);
      toast.success(`Unblocked ${confirmUnblock.displayName}`);
      setConfirmUnblock(null);

      // Remove from list
      setBlockedUsers(blockedUsers.filter((u) => u.userId !== confirmUnblock.userId));
    } catch (err: any) {
      console.error('Unblock error:', err);
      toast.error(err.message ?? 'Failed to unblock user');
      setConfirmUnblock(null);
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2 flex items-center gap-2">
          <UserX className="w-6 h-6" />
          Blocked Users
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Manage users you've blocked. Blocked users can't message you or see your profile.
        </p>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-12">
          <div className="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-light-text-secondary dark:text-dark-text-secondary">
            Loading blocked users...
          </p>
        </div>
      )}

      {/* Empty State */}
      {!loading && blockedUsers.length === 0 && (
        <div className="text-center py-16">
          <div className="w-16 h-16 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mx-auto mb-4">
            <ShieldOff className="w-8 h-8 text-gray-500 dark:text-gray-400" />
          </div>
          <h3 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary mb-2">
            No blocked users
          </h3>
          <p className="text-light-text-secondary dark:text-dark-text-secondary max-w-md mx-auto">
            You haven't blocked anyone yet. You can block users from their profile or search results.
          </p>
        </div>
      )}

      {/* Blocked Users List */}
      {!loading && blockedUsers.length > 0 && (
        <div>
          <div className="mb-4 text-sm text-light-text-secondary dark:text-dark-text-secondary">
            {blockedUsers.length} {blockedUsers.length === 1 ? 'user' : 'users'} blocked
          </div>
          <div className="space-y-3">
            {blockedUsers.map((user) => (
              <div
                key={user.id}
                className="flex items-center justify-between p-4 bg-light-surface dark:bg-dark-surface rounded-lg border border-light-border dark:border-dark-border"
              >
                {/* User Info */}
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <Avatar
                    src={user.avatarUrl}
                    alt={user.displayName}
                    size="md"
                    fallback={user.displayName}
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-light-text-primary dark:text-dark-text-primary truncate">
                      {user.displayName}
                    </p>
                    <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                      Blocked {new Date(user.blockedAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>

                {/* Unblock Button */}
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setConfirmUnblock(user)}
                  className="gap-2 flex-shrink-0 ml-3"
                >
                  <ShieldOff className="w-4 h-4" />
                  Unblock
                </Button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Unblock Confirmation Modal */}
      <Modal
        isOpen={!!confirmUnblock}
        onClose={() => setConfirmUnblock(null)}
        title="Unblock User"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary dark:text-dark-text-secondary">
            Are you sure you want to unblock{' '}
            <strong className="text-light-text-primary dark:text-dark-text-primary">
              {confirmUnblock?.displayName}
            </strong>
            ? They will be able to:
          </p>
          <ul className="list-disc list-inside text-sm text-light-text-secondary dark:text-dark-text-secondary space-y-1 ml-2">
            <li>Send you messages</li>
            <li>Send you connection requests</li>
            <li>See your profile in search results</li>
          </ul>
          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setConfirmUnblock(null)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              fullWidth
              onClick={handleUnblock}
              className="gap-2"
            >
              <ShieldOff className="w-4 h-4" />
              Unblock User
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
