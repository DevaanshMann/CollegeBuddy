import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, MessageCircle, UserPlus, UserMinus } from 'lucide-react';
import { apiClient } from '../../api/client';
import type { DashboardStats, ConnectionRequestDto } from '../../types';
import { Avatar, Button, Modal } from '../../components/ui';
import { clsx } from 'clsx';
import toast from 'react-hot-toast';

type Friend = {
  userId: number;
  displayName: string;
  avatarUrl?: string;
  unreadCount?: number;
};

export function HomePage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats>({
    pendingRequestsCount: 0,
    unreadMessagesCount: 0,
    connectionsCount: 0,
    profileViews: 0,
  });
  const [incomingRequests, setIncomingRequests] = useState<ConnectionRequestDto[]>([]);
  const [friends, setFriends] = useState<Friend[]>([]);
  const [loading, setLoading] = useState(true);
  const [confirmDisconnect, setConfirmDisconnect] = useState<{
    userId: number;
    displayName: string;
  } | null>(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const [connectionsData] = await Promise.all([
        apiClient.get<{
          connections: any[];
          incomingRequests: ConnectionRequestDto[];
          outgoingRequests: any[];
          unreadCounts: Record<number, number>;
        }>('/connections'),
      ]);

      setIncomingRequests(connectionsData.incomingRequests || []);

      // Map connections to friends format
      const friendsList = (connectionsData.connections || []).map((conn: any) => ({
        userId: conn.userId,
        displayName: conn.displayName,
        avatarUrl: conn.avatarUrl,
        unreadCount: connectionsData.unreadCounts?.[conn.userId] || 0,
      }));
      setFriends(friendsList);

      // Calculate number of people with unread messages (not total unread count)
      const peopleWithUnreadMessages = Object.values(connectionsData.unreadCounts || {}).filter(
        (count: number) => count > 0
      ).length;

      setStats({
        pendingRequestsCount: (connectionsData.incomingRequests || []).length,
        unreadMessagesCount: peopleWithUnreadMessages,
        connectionsCount: (connectionsData.connections || []).length,
        profileViews: 0,
      });
    } catch (error) {
      console.error('Failed to load dashboard:', error);
      toast.error('Failed to load dashboard data');
      // Set empty data to prevent crashes
      setIncomingRequests([]);
      setFriends([]);
      setStats({
        pendingRequestsCount: 0,
        unreadMessagesCount: 0,
        connectionsCount: 0,
        profileViews: 0,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleAcceptRequest = async (userId: number) => {
    try {
      await apiClient.post('/connections/respond', {
        otherUserId: userId,
        accept: true,
      });
      loadDashboard(); // Reload to update stats
    } catch (error) {
      console.error('Failed to accept request:', error);
    }
  };

  const handleDeclineRequest = async (userId: number) => {
    try {
      await apiClient.post('/connections/respond', {
        otherUserId: userId,
        accept: false,
      });
      loadDashboard(); // Reload to update stats
    } catch (error) {
      console.error('Failed to decline request:', error);
    }
  };

  const handleDisconnect = async () => {
    if (!confirmDisconnect) return;

    try {
      await apiClient.del(`/connections/${confirmDisconnect.userId}`);
      toast.success(`Disconnected from ${confirmDisconnect.displayName}`);
      setConfirmDisconnect(null);
      loadDashboard(); // Reload to update stats
    } catch (err: any) {
      console.error('Disconnect error:', err);
      toast.error(err.message ?? 'Failed to disconnect');
      setConfirmDisconnect(null);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  const statCards = [
    {
      title: 'Connections',
      value: stats.connectionsCount,
      icon: Users,
      color: 'text-blue-500',
      bgColor: 'bg-blue-50',
      onClick: () => navigate('/connections?view=connections'),
    },
    {
      title: 'Unread Messages',
      value: stats.unreadMessagesCount,
      icon: MessageCircle,
      color: 'text-green-500',
      bgColor: 'bg-green-50',
      onClick: () => navigate('/messages'),
    },
    {
      title: 'Pending Requests',
      value: stats.pendingRequestsCount,
      icon: UserPlus,
      color: 'text-orange-500',
      bgColor: 'bg-orange-50',
      onClick: () => navigate('/connections?view=requests'),
    },
  ];

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-light-text-primary mb-2">
          Welcome back!
        </h1>
        <p className="text-light-text-secondary">
          Here's what's happening with your CollegeBuddy account
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <button
              key={card.title}
              onClick={card.onClick}
              className="card p-6 hover:shadow-md transition-shadow cursor-pointer text-left"
            >
              <div className="flex items-start justify-between mb-4">
                <div className={clsx('p-3 rounded-xl', card.bgColor)}>
                  <Icon className={clsx('w-6 h-6', card.color)} />
                </div>
              </div>
              <h3 className="text-2xl font-bold text-light-text-primary mb-1">
                {card.value}
              </h3>
              <p className="text-sm text-light-text-secondary">
                {card.title}
              </p>
            </button>
          );
        })}
      </div>

      {/* Disconnect Confirmation Modal */}
      <Modal
        isOpen={!!confirmDisconnect}
        onClose={() => setConfirmDisconnect(null)}
        title="Disconnect from connection"
      >
        <div className="space-y-4">
          <p className="text-light-text-secondary">
            Are you sure you want to disconnect from{' '}
            <strong className="text-light-text-primary">
              {confirmDisconnect?.displayName}
            </strong>
            ? You will need to send a new connection request to reconnect.
          </p>
          <div className="flex gap-3">
            <Button
              variant="secondary"
              fullWidth
              onClick={() => setConfirmDisconnect(null)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              fullWidth
              onClick={handleDisconnect}
              className="gap-2"
            >
              <UserMinus className="w-4 h-4" />
              Disconnect
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
