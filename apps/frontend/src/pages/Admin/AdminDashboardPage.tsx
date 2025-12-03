import { useEffect, useState } from 'react';
import { Users, UserCheck, UserX, Clock, MessageSquare, Link, ChevronLeft, ChevronRight, Shield } from 'lucide-react';
import { adminApi } from '../../api/admin';
import type { AdminUserDto, AdminStatsDto, AccountStatus } from '../../api/admin';
import { Avatar, Button, Modal } from '../../components/ui';
import toast from 'react-hot-toast';

export function AdminDashboardPage() {
  const [stats, setStats] = useState<AdminStatsDto | null>(null);
  const [users, setUsers] = useState<AdminUserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedUser, setSelectedUser] = useState<AdminUserDto | null>(null);
  const [newStatus, setNewStatus] = useState<AccountStatus | null>(null);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    loadData();
  }, [page]);

  async function loadData() {
    setLoading(true);
    try {
      const [statsData, usersData] = await Promise.all([
        adminApi.getStats(),
        adminApi.getUsers(page, 10, 'id'),
      ]);

      setStats(statsData);
      setUsers(usersData.content);
      setTotalPages(usersData.totalPages);
    } catch (err: any) {
      console.error('Failed to load admin data:', err);
      toast.error(err.message ?? 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateStatus() {
    if (!selectedUser || !newStatus) return;

    setUpdating(true);
    try {
      await adminApi.updateUserStatus(selectedUser.userId, newStatus);
      toast.success(`User status updated to ${newStatus}`);
      setSelectedUser(null);
      setNewStatus(null);
      loadData(); // Reload data
    } catch (err: any) {
      console.error('Failed to update status:', err);
      toast.error(err.message ?? 'Failed to update user status');
    } finally {
      setUpdating(false);
    }
  }

  function openStatusModal(user: AdminUserDto) {
    setSelectedUser(user);
    setNewStatus(user.status);
  }

  const getStatusColor = (status: AccountStatus) => {
    switch (status) {
      case 'ACTIVE':
        return 'text-green-600 dark:text-green-400 bg-green-100 dark:bg-green-900/30';
      case 'PENDING_VERIFICATION':
        return 'text-yellow-600 dark:text-yellow-400 bg-yellow-100 dark:bg-yellow-900/30';
      case 'DEACTIVATED':
        return 'text-red-600 dark:text-red-400 bg-red-100 dark:bg-red-900/30';
      default:
        return 'text-gray-600 dark:text-gray-500 dark:text-gray-400 bg-gray-100 dark:bg-gray-900/30';
    }
  };

  const getRoleBadge = (role: string) => {
    if (role === 'ADMIN') {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-semibold rounded-full bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300">
          <Shield className="w-3 h-3" />
          Admin
        </span>
      );
    }
    return null;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-light-text-primary dark:text-dark-text-primary mb-2 flex items-center gap-2">
          <Shield className="w-8 h-8 text-purple-500" />
          Admin Dashboard
        </h1>
        <p className="text-light-text-secondary dark:text-dark-text-secondary">
          Manage users and monitor platform activity
        </p>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Total Users
                </p>
                <p className="text-3xl font-bold text-light-text-primary dark:text-dark-text-primary">
                  {stats.totalUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                <Users className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </div>

          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Active Users
                </p>
                <p className="text-3xl font-bold text-green-600 dark:text-green-400">
                  {stats.activeUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
                <UserCheck className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </div>

          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Pending Verification
                </p>
                <p className="text-3xl font-bold text-yellow-600 dark:text-yellow-400">
                  {stats.pendingVerificationUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-yellow-100 dark:bg-yellow-900/30 rounded-full flex items-center justify-center">
                <Clock className="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
              </div>
            </div>
          </div>

          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Deactivated
                </p>
                <p className="text-3xl font-bold text-red-600 dark:text-red-400">
                  {stats.deactivatedUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
                <UserX className="w-6 h-6 text-red-600 dark:text-red-400" />
              </div>
            </div>
          </div>

          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Total Connections
                </p>
                <p className="text-3xl font-bold text-light-text-primary dark:text-dark-text-primary">
                  {stats.totalConnections}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                <Link className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </div>

          <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary mb-1">
                  Total Messages
                </p>
                <p className="text-3xl font-bold text-light-text-primary dark:text-dark-text-primary">
                  {stats.totalMessages}
                </p>
              </div>
              <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-full flex items-center justify-center">
                <MessageSquare className="w-6 h-6 text-purple-600 dark:text-purple-400" />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Users Table */}
      <div className="bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-light-border dark:border-dark-border">
          <h2 className="text-lg font-semibold text-light-text-primary dark:text-dark-text-primary">
            User Management
          </h2>
        </div>

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            <p className="mt-4 text-light-text-secondary dark:text-dark-text-secondary">
              Loading users...
            </p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 dark:bg-gray-800/50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      User
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      Campus
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      Role
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-light-text-secondary dark:text-dark-text-secondary uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-light-border dark:divide-dark-border">
                  {users.map((user) => (
                    <tr key={user.userId} className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-3">
                          <Avatar
                            src={user.avatarUrl}
                            alt={user.displayName}
                            size="sm"
                            fallback={user.displayName}
                          />
                          <div>
                            <p className="font-medium text-light-text-primary dark:text-dark-text-primary">
                              {user.displayName}
                            </p>
                            <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                              ID: {user.userId}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <p className="text-sm text-light-text-primary dark:text-dark-text-primary">
                          {user.email}
                        </p>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <p className="text-sm text-light-text-primary dark:text-dark-text-primary">
                          {user.campusDomain}
                        </p>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(user.status)}`}>
                          {user.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getRoleBadge(user.role)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Button
                          variant="secondary"
                          size="sm"
                          onClick={() => openStatusModal(user)}
                        >
                          Edit Status
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="px-6 py-4 border-t border-light-border dark:border-dark-border flex items-center justify-between">
              <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                Page {page + 1} of {totalPages}
              </p>
              <div className="flex gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                  className="gap-1"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                  className="gap-1"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Update Status Modal */}
      <Modal
        isOpen={!!selectedUser}
        onClose={() => {
          setSelectedUser(null);
          setNewStatus(null);
        }}
        title="Update User Status"
      >
        {selectedUser && (
          <div className="space-y-4">
            <div className="flex items-center gap-3 p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
              <Avatar
                src={selectedUser.avatarUrl}
                alt={selectedUser.displayName}
                size="md"
                fallback={selectedUser.displayName}
              />
              <div>
                <p className="font-semibold text-light-text-primary dark:text-dark-text-primary">
                  {selectedUser.displayName}
                </p>
                <p className="text-sm text-light-text-secondary dark:text-dark-text-secondary">
                  {selectedUser.email}
                </p>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-light-text-primary dark:text-dark-text-primary mb-2">
                Account Status
              </label>
              <select
                value={newStatus || ''}
                onChange={(e) => setNewStatus(e.target.value as AccountStatus)}
                className="w-full px-3 py-2 bg-light-surface dark:bg-dark-surface border border-light-border dark:border-dark-border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-light-text-primary dark:text-dark-text-primary"
              >
                <option value="ACTIVE">Active</option>
                <option value="PENDING_VERIFICATION">Pending Verification</option>
                <option value="DEACTIVATED">Deactivated</option>
              </select>
            </div>

            <div className="flex gap-3">
              <Button
                variant="secondary"
                fullWidth
                onClick={() => {
                  setSelectedUser(null);
                  setNewStatus(null);
                }}
                disabled={updating}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                fullWidth
                onClick={handleUpdateStatus}
                loading={updating}
              >
                Update Status
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
